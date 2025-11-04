package com.courtvision.scheduler;

import com.courtvision.entity.League;
import com.courtvision.repository.LeagueRepository;
import com.courtvision.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job for calculating league scores
 * Runs once daily at configured time (default: 2 AM UTC)
 *
 * The NBA API updates every 60 seconds, so we calculate scores once per day
 * to provide a stable daily snapshot of team performance
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScoreCalculationScheduler {

    private final ScoreService scoreService;
    private final LeagueRepository leagueRepository;

    /**
     * Calculate scores for all active leagues once per day
     * Scheduled to run at 2 AM UTC (configurable via properties)
     *
     * Cron format: second minute hour day month weekday
     * "0 0 2 * * ?" = 2:00 AM every day
     */
    @Scheduled(cron = "${scheduling.score-calculation.cron:0 0 2 * * ?}")
    public void calculateDailyScores() {
        log.info("Starting daily score calculation job at {}", LocalDateTime.now());

        try {
            // Get all active leagues
            List<League> leagues = leagueRepository.findAll();
            log.info("Found {} leagues to calculate scores for", leagues.size());

            int successCount = 0;
            int errorCount = 0;

            for (League league : leagues) {
                try {
                    // Check if scores already calculated today
                    if (scoreService.scoresCalculatedToday(league.getId())) {
                        log.debug("Scores already calculated today for league: {}", league.getName());
                        continue;
                    }

                    // Calculate scores for this league
                    scoreService.calculateLeagueScores(league.getId());
                    successCount++;

                    log.info("Successfully calculated scores for league: {}", league.getName());

                } catch (Exception e) {
                    errorCount++;
                    log.error("Error calculating scores for league: {}", league.getName(), e);
                }
            }

            log.info("Completed daily score calculation: {} successful, {} failed", successCount, errorCount);

        } catch (Exception e) {
            log.error("Fatal error in score calculation scheduler", e);
        }
    }

    /**
     * Optional: Recalculate scores for a specific league
     * Can be called manually via endpoint if needed
     * @param leagueId The league ID
     */
    public void recalculateLeagueScores(Long leagueId) {
        log.info("Manually triggering score recalculation for league: {}", leagueId);
        try {
            scoreService.calculateLeagueScores(leagueId);
            log.info("Successfully recalculated scores for league: {}", leagueId);
        } catch (Exception e) {
            log.error("Error recalculating scores for league: {}", leagueId, e);
            throw e;
        }
    }
}
