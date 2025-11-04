package com.courtvision.config;

import com.courtvision.dto.ScoreUpdateEvent;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Test configuration for Kafka - mocks Kafka template to avoid connection issues during tests
 */
@TestConfiguration
public class TestKafkaConfig {

    /**
     * Provide a mock KafkaTemplate for tests
     */
    @Bean
    @Primary
    public KafkaTemplate<String, ScoreUpdateEvent> scoreUpdateKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    /**
     * Provide a mock String KafkaTemplate for tests
     */
    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }
}
