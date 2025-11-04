package com.courtvision.kafka;

import com.courtvision.config.KafkaConfig;
import com.courtvision.controller.LeaderboardController;
import com.courtvision.dto.ScoreCalculationDTO;
import com.courtvision.dto.ScoreUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Kafka consumer for score update events
 * Listens to league-scores-updated topic and broadcasts to WebSocket clients
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreUpdateConsumer {

    private final LeaderboardController leaderboardController;

    /**
     * Consume score update events from Kafka and broadcast to WebSocket
     * Called when scores are updated via the scheduled job or manual trigger
     * @param event The score update event
     */
    @KafkaListener(
            topics = KafkaConfig.LEAGUE_SCORES_TOPIC,
            groupId = "courtvision-score-consumer-group"
    )
    public void consumeScoreUpdate(ScoreUpdateEvent event) {
        log.info("Received score update: League={}, User={}, Score={}, Rank={}",
                event.getLeagueName(),
                event.getUsername(),
                event.getTotalScore(),
                event.getRank());

        try {
            // Convert Kafka event to DTO for WebSocket broadcast
            ScoreCalculationDTO scoreDTO = ScoreCalculationDTO.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .totalScore(event.getTotalScore())
                    .averageScore(event.getAverageScore())
                    .playersEvaluated(event.getPlayersEvaluated())
                    .calculatedAt(event.getCalculatedAt())
                    .build();

            List<ScoreCalculationDTO> scoreList = List.of(scoreDTO);

            // Broadcast to WebSocket clients via LeaderboardController
            leaderboardController.broadcastScoreUpdate(
                    event.getLeagueId(),
                    event.getLeagueName(),
                    scoreList
            );

            log.debug("Broadcast score update via WebSocket for league: {}", event.getLeagueId());

        } catch (Exception e) {
            log.error("Error processing score update event", e);
        }
    }
}
