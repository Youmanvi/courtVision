package com.courtvision.config;

import com.courtvision.dto.ScoreUpdateEvent;
import com.courtvision.kafka.WinnerAnnouncementEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for score update events
 * Configures topics, producers, and consumers for league score updates
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String LEAGUE_SCORES_TOPIC = "league-scores-updated";
    public static final String SCORE_CALCULATION_TOPIC = "score-calculation-requests";
    public static final String LEAGUE_WINNERS_TOPIC = "league-winners-announced";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:courtvision-group}")
    private String groupId;

    /**
     * Kafka Admin bean for topic management
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Create league-scores-updated topic
     */
    @Bean
    public NewTopic leagueScoresTopic() {
        return TopicBuilder.name(LEAGUE_SCORES_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create score-calculation-requests topic
     */
    @Bean
    public NewTopic scoreCalculationTopic() {
        return TopicBuilder.name(SCORE_CALCULATION_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Create league-winners-announced topic
     */
    @Bean
    public NewTopic leagueWinnersTopic() {
        return TopicBuilder.name(LEAGUE_WINNERS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Producer factory for ScoreUpdateEvent messages
     */
    @Bean
    public ProducerFactory<String, ScoreUpdateEvent> scoreUpdateProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending ScoreUpdateEvent messages
     */
    @Bean
    public KafkaTemplate<String, ScoreUpdateEvent> scoreUpdateKafkaTemplate() {
        return new KafkaTemplate<>(scoreUpdateProducerFactory());
    }

    /**
     * Consumer factory for ScoreUpdateEvent messages
     */
    @Bean
    public ConsumerFactory<String, ScoreUpdateEvent> scoreUpdateConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ScoreUpdateEvent.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener container factory for ScoreUpdateEvent consumers
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ScoreUpdateEvent>>
    scoreUpdateKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ScoreUpdateEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(scoreUpdateConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }

    /**
     * Producer factory for String messages
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending String messages
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Producer factory for WinnerAnnouncementEvent messages
     */
    @Bean
    public ProducerFactory<String, WinnerAnnouncementEvent> winnerAnnouncementProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending WinnerAnnouncementEvent messages
     */
    @Bean
    public KafkaTemplate<String, WinnerAnnouncementEvent> winnerAnnouncementKafkaTemplate() {
        return new KafkaTemplate<>(winnerAnnouncementProducerFactory());
    }

    /**
     * Consumer factory for WinnerAnnouncementEvent messages
     */
    @Bean
    public ConsumerFactory<String, WinnerAnnouncementEvent> winnerAnnouncementConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "courtvision-winner-consumer-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WinnerAnnouncementEvent.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener container factory for WinnerAnnouncementEvent consumers
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, WinnerAnnouncementEvent>>
    winnerAnnouncementKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, WinnerAnnouncementEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(winnerAnnouncementConsumerFactory());
        factory.setConcurrency(1);
        return factory;
    }
}
