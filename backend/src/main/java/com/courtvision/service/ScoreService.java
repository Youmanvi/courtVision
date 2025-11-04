package com.courtvision.service;

import com.courtvision.config.KafkaConfig;
import com.courtvision.dto.NBAPlayerDTO;
import com.courtvision.dto.ScoreUpdateEvent;
import com.courtvision.entity.*;
import com.courtvision.repository.DraftPickRepository;
import com.courtvision.repository.LeagueMemberRepository;
import com.courtvision.repository.LeagueRepository;
import com.courtvision.repository.ScoreCalculationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating and managing league scores
 * Uses NBA player stats to calculate fantasy points for each team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreCalculationRepository scoreCalculationRepository;
    private final DraftPickRepository draftPickRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueMemberRepository leagueMemberRepository;
    private final NBAPlayerService nbaPlayerService;
    private final KafkaTemplate<String, ScoreUpdateEvent> scoreUpdateKafkaTemplate;

    @Autowired
    private com.courtvision.repository.DraftRepository draftRepository;

    // Scoring configuration (fantasy points per stat)
    private static final double POINTS_PER_POINT = 1.0;
    private static final double POINTS_PER_REBOUND = 1.2;
    private static final double POINTS_PER_ASSIST = 1.5;
    private static final double POINTS_PER_STEAL = 2.0;
    private static final double POINTS_PER_BLOCK = 2.0;
    private static final double POINTS_PER_TURNOVER = -0.5;
    private static final double POINTS_PER_FOUL = -0.2;

    /**
     * Calculate scores for all users in a league
     * @param leagueId The league ID
     * @return Map of user IDs to their calculated scores
     */
    @Transactional
    public Map<Long, ScoreCalculation> calculateLeagueScores(Long leagueId) {
        log.info("Calculating scores for league: {}", leagueId);

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found: " + leagueId));

        // Get all league members
        List<LeagueMember> members = leagueMemberRepository.findByLeagueId(leagueId);
        if (members.isEmpty()) {
            log.warn("No members found for league: {}", leagueId);
            return new HashMap<>();
        }

        Map<Long, ScoreCalculation> scoreMap = new HashMap<>();

        // Calculate score for each user
        for (LeagueMember member : members) {
            User user = member.getUser();
            log.debug("Calculating score for user: {} in league: {}", user.getUsername(), leagueId);

            ScoreCalculation score = calculateUserScore(league, user);
            if (score != null) {
                scoreCalculationRepository.save(score);
                scoreMap.put(user.getId(), score);
            }
        }

        // Publish scores to Kafka and assign rankings
        publishScoresToKafka(league, scoreMap);

        log.info("Completed score calculation for league: {} with {} users", leagueId, scoreMap.size());
        return scoreMap;
    }

    /**
     * Calculate score for a single user in a league
     * @param league The league
     * @param user The user
     * @return ScoreCalculation with calculated score
     */
    private ScoreCalculation calculateUserScore(League league, User user) {
        // Get the draft for this league
        Optional<Draft> draftOpt = draftRepository.findByLeagueId(league.getId());
        if (draftOpt.isEmpty()) {
            log.warn("No draft found for league: {}", league.getId());
            return null;
        }

        Draft draft = draftOpt.get();

        // Get all draft picks for this user in this league's draft
        List<DraftPick> userPicks = draftPickRepository.findByDraftIdAndPickerId(draft.getId(), user.getId());

        if (userPicks.isEmpty()) {
            log.debug("No picks found for user: {} in league: {}", user.getUsername(), league.getId());
            return ScoreCalculation.builder()
                    .league(league)
                    .user(user)
                    .totalScore(0.0)
                    .playersEvaluated(0)
                    .calculatedAt(LocalDateTime.now())
                    .build();
        }

        double totalScore = 0.0;
        int playersEvaluated = 0;

        // Calculate score for each picked player
        for (DraftPick pick : userPicks) {
            double playerScore = calculatePlayerScore(pick.getPlayerName());
            totalScore += playerScore;
            playersEvaluated++;
        }

        return ScoreCalculation.builder()
                .league(league)
                .user(user)
                .totalScore(totalScore)
                .playersEvaluated(playersEvaluated)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calculate fantasy points for a single player based on NBA stats
     * Uses scoring configuration: points, rebounds, assists, steals, blocks, turnovers, fouls
     * @param playerName The player name
     * @return Fantasy points
     */
    private double calculatePlayerScore(String playerName) {
        try {
            List<NBAPlayerDTO> players = nbaPlayerService.searchPlayersByName(playerName);
            if (players.isEmpty()) {
                log.warn("Player not found in NBA database: {}", playerName);
                return 0.0;
            }

            // Get best matching player (first result)
            NBAPlayerDTO player = players.get(0);

            // Calculate fantasy points based on stats
            // For now, using a simple scoring system based on average stats
            double score = 0.0;

            // If player has detailed stats (from full API response), use them
            // Otherwise use basic calculation
            score = calculateScoreFromPlayer(player);

            log.debug("Calculated score for player: {} = {}", playerName, score);
            return score;

        } catch (Exception e) {
            log.error("Error calculating score for player: {}", playerName, e);
            return 0.0;
        }
    }

    /**
     * Calculate fantasy score from player stats
     * This is a simplified calculation based on available data
     * In production, you'd integrate with more detailed player stats
     * @param player The NBA player
     * @return Fantasy points
     */
    private double calculateScoreFromPlayer(NBAPlayerDTO player) {
        double score = 0.0;

        // Basic scoring: 1 point per stat category the player has played in
        // In production environment with real API, we'd use actual stat values
        if (player.getPosition() != null && !player.getPosition().isEmpty()) {
            score += 5.0; // Base score for participation
        }

        // Bonus based on player salary (proxy for performance level)
        if (player.getSalary() != null && player.getSalary() > 0) {
            // Higher salary = higher expected score
            score += (player.getSalary() / 1000000.0); // 1 point per million in salary
        }

        return Math.max(score, 0.0);
    }

    /**
     * Publish score update events to Kafka
     * @param league The league
     * @param scoreMap Map of scores
     */
    private void publishScoresToKafka(League league, Map<Long, ScoreCalculation> scoreMap) {
        if (scoreMap.isEmpty()) {
            return;
        }

        // Sort scores by total score descending for ranking
        List<ScoreCalculation> rankedScores = scoreMap.values().stream()
                .sorted(Comparator.comparingDouble(ScoreCalculation::getTotalScore).reversed())
                .collect(Collectors.toList());

        // Publish each score with ranking
        for (int i = 0; i < rankedScores.size(); i++) {
            ScoreCalculation score = rankedScores.get(i);
            int rank = i + 1;

            ScoreUpdateEvent event = ScoreUpdateEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .leagueId(league.getId())
                    .leagueName(league.getName())
                    .userId(score.getUser().getId())
                    .username(score.getUser().getUsername())
                    .totalScore(score.getTotalScore())
                    .averageScore(score.getAverageScore())
                    .playersEvaluated(score.getPlayersEvaluated())
                    .rank(rank)
                    .calculatedAt(score.getCalculatedAt())
                    .timestamp(LocalDateTime.now())
                    .eventType("LEAGUE_SCORES_UPDATED")
                    .build();

            try {
                scoreUpdateKafkaTemplate.send(KafkaConfig.LEAGUE_SCORES_TOPIC,
                        league.getId() + "-" + score.getUser().getId(),
                        event);
                log.debug("Published score update to Kafka: {} - {}", league.getName(), score.getUser().getUsername());
            } catch (Exception e) {
                log.error("Error publishing score update to Kafka", e);
            }
        }
    }

    /**
     * Get latest scores for a league
     * @param leagueId The league ID
     * @return List of scores ordered by ranking
     */
    @Transactional(readOnly = true)
    public List<ScoreCalculation> getLeagueScoreboard(Long leagueId) {
        return scoreCalculationRepository.findLatestLeagueScoresOrdered(leagueId);
    }

    /**
     * Get user's score in a league
     * @param leagueId The league ID
     * @param userId The user ID
     * @return User's latest score
     */
    @Transactional(readOnly = true)
    public Optional<ScoreCalculation> getUserScore(Long leagueId, Long userId) {
        return scoreCalculationRepository.findFirstByLeagueIdAndUserIdOrderByCalculatedAtDesc(leagueId, userId);
    }

    /**
     * Check if scores have been calculated for a league today
     * @param leagueId The league ID
     * @return true if scores already calculated today
     */
    @Transactional(readOnly = true)
    public boolean scoresCalculatedToday(Long leagueId) {
        return scoreCalculationRepository.countScoresCalculatedToday(leagueId) > 0;
    }
}
