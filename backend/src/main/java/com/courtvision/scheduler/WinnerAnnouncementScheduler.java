package com.courtvision.scheduler;

import com.courtvision.entity.League;
import com.courtvision.entity.League.LeagueStatus;
import com.courtvision.entity.LeagueWinner;
import com.courtvision.entity.ScoreCalculation;
import com.courtvision.entity.TransactionStatus;
import com.courtvision.kafka.WinnerAnnouncementEvent;
import com.courtvision.repository.LeagueRepository;
import com.courtvision.repository.LeagueWinnerRepository;
import com.courtvision.repository.ScoreCalculationRepository;
import com.courtvision.service.SolanaOracleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Winner Announcement Scheduler
 * Announces league winners on June 7th and submits them to Solana blockchain
 *
 * Default schedule: 00:00 UTC on June 7th every year (0 0 0 7 6 ?)
 * This can be overridden via: scheduling.winner-announcement.cron
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WinnerAnnouncementScheduler {

    private final LeagueRepository leagueRepository;
    private final LeagueWinnerRepository leagueWinnerRepository;
    private final ScoreCalculationRepository scoreCalculationRepository;
    private final SolanaOracleService solanaOracleService;
    private final KafkaTemplate<String, WinnerAnnouncementEvent> kafkaTemplate;

    /**
     * Announce winners on June 7th at 00:00 UTC
     * Cron: "0 0 0 7 6 ?" (June 7th at midnight)
     * Configurable via: scheduling.winner-announcement.cron
     */
    @Scheduled(cron = "${scheduling.winner-announcement.cron:0 0 0 7 6 ?}")
    @Transactional
    public void announceWinnersOnJune7th() {
        try {
            log.info("=== Winner Announcement Scheduled Task Started ===");

            // Get all active leagues - note: repository doesn't have findByStatus, so we get all and filter
            List<League> allLeagues = leagueRepository.findAll();
            List<League> activeLeagues = allLeagues.stream()
                .filter(l -> l.getStatus() == LeagueStatus.ACTIVE)
                .toList();
            log.info("Found {} active leagues", activeLeagues.size());

            int successCount = 0;
            int failureCount = 0;

            for (League league : activeLeagues) {
                try {
                    // Check if winner already announced for this league
                    if (leagueWinnerRepository.existsByLeagueId(league.getId())) {
                        log.debug("Winner already announced for league: {}", league.getName());
                        continue;
                    }

                    // Get latest scores for league (ordered by score descending)
                    List<ScoreCalculation> leagueScores = scoreCalculationRepository.findLatestLeagueScoresOrdered(league.getId());

                    if (leagueScores.isEmpty()) {
                        log.warn("No scores found for league: {}", league.getName());
                        failureCount++;
                        continue;
                    }

                    // Winner is the user with the highest score (first in ordered list)
                    ScoreCalculation winnerScore = leagueScores.get(0);
                    announceWinner(league, winnerScore, leagueScores.size());
                    successCount++;

                } catch (Exception e) {
                    log.error("Error announcing winner for league: {}", league.getName(), e);
                    failureCount++;
                }
            }

            log.info("=== Winner Announcement Task Complete - Success: {}, Failures: {} ===", successCount, failureCount);

        } catch (Exception e) {
            log.error("Error in winner announcement scheduler", e);
        }
    }

    /**
     * Manually trigger winner announcement (for testing/admin)
     */
    @Transactional
    public void announceWinnerForLeague(Long leagueId) {
        try {
            log.info("Manually announcing winner for league: {}", leagueId);

            League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found: " + leagueId));

            // Get latest scores for league
            List<ScoreCalculation> leagueScores = scoreCalculationRepository.findLatestLeagueScoresOrdered(leagueId);

            if (leagueScores.isEmpty()) {
                throw new IllegalArgumentException("No scores found for league: " + leagueId);
            }

            // Winner is the first score (highest)
            ScoreCalculation winnerScore = leagueScores.get(0);
            announceWinner(league, winnerScore, leagueScores.size());

            log.info("Successfully announced winner for league: {}", leagueId);

        } catch (Exception e) {
            log.error("Error announcing winner for league: {}", leagueId, e);
            throw new RuntimeException("Error announcing winner", e);
        }
    }

    /**
     * Internal method to announce a winner and submit to blockchain
     */
    private void announceWinner(League league, ScoreCalculation winnerScore, int totalParticipants) {
        try {
            log.info("Announcing winner - League: {}, Winner: {}, Score: {}",
                league.getName(), winnerScore.getUser().getUsername(), winnerScore.getTotalScore());

            // Validate wallet address
            String walletAddress = winnerScore.getUser().getSolanaWallet();
            if (walletAddress == null || !solanaOracleService.isValidSolanaAddress(walletAddress)) {
                log.error("Invalid wallet address for winner: {}", walletAddress);
                return;
            }

            // Create LeagueWinner record
            LeagueWinner winner = LeagueWinner.builder()
                .league(league)
                .winner(winnerScore.getUser())
                .finalScore(winnerScore.getTotalScore())
                .rank(1) // First place
                .solanaWallet(walletAddress)
                .transactionStatus(TransactionStatus.PENDING)
                .announcedAt(LocalDateTime.now())
                .build();

            LeagueWinner savedWinner = leagueWinnerRepository.save(winner);
            log.info("Created LeagueWinner record: ID={}", savedWinner.getId());

            // Create and publish Kafka event
            WinnerAnnouncementEvent event = createWinnerAnnouncementEvent(league, winnerScore, winnerScore.getUser().getSolanaWallet(), totalParticipants);
            publishWinnerEvent(event);

            log.info("Winner announcement complete for league: {}", league.getName());

        } catch (Exception e) {
            log.error("Error announcing winner", e);
        }
    }

    /**
     * Create winner announcement Kafka event
     */
    private WinnerAnnouncementEvent createWinnerAnnouncementEvent(
        League league,
        ScoreCalculation scoreCalculation,
        String solanaWallet,
        int totalParticipants
    ) {
        return WinnerAnnouncementEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("LEAGUE_WINNER_ANNOUNCED")
            .leagueId(league.getId())
            .leagueName(league.getName())
            .winnerId(scoreCalculation.getUser().getId())
            .winnerUsername(scoreCalculation.getUser().getUsername())
            .solanaWallet(solanaWallet)
            .finalScore(scoreCalculation.getTotalScore())
            .rank(1)
            .totalParticipants(totalParticipants)
            .announcedAt(LocalDateTime.now())
            .transactionStatus("PENDING")
            .network(solanaOracleService.getNetwork())
            .build();
    }

    /**
     * Publish winner announcement event to Kafka
     */
    private void publishWinnerEvent(WinnerAnnouncementEvent event) {
        try {
            kafkaTemplate.send("league-winners-announced", event.getEventId(), event);
            log.debug("Published winner announcement event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Error publishing winner announcement event", e);
        }
    }
}
