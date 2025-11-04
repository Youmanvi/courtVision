package com.courtvision.kafka;

import com.courtvision.dto.ScoreUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for score update events
 * Listens to league-scores-updated topic and processes score events
 */
@Slf4j
@Service
public class ScoreUpdateConsumer {

    /**
     * Consume score update events from Kafka
     * In a real application, this could:
     * - Broadcast to WebSocket subscribers
     * - Update real-time caches
     * - Trigger notifications
     * - Log metrics
     * @param event The score update event
     */
    @KafkaListener(
        topics = "league-scores-updated",
        groupId = "score-update-listeners",
        containerFactory = "scoreUpdateKafkaListenerContainerFactory"
    )
    public void consumeScoreUpdate(ScoreUpdateEvent event) {
        log.info("Received score update: League={}, User={}, Score={}, Rank={}",
                event.getLeagueName(),
                event.getUsername(),
                event.getTotalScore(),
                event.getRank());

        // TODO: Broadcast to WebSocket subscribers for real-time UI updates
        // TODO: Update real-time score cache
        // TODO: Send notifications if needed
        // TODO: Log event for analytics
    }
}
