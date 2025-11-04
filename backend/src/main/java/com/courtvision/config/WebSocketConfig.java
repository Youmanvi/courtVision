package com.courtvision.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time leaderboard updates
 * Enables STOMP messaging protocol over WebSocket
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Register STOMP endpoints for client connections
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/scores")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:8080")
                .withSockJS();
    }

    /**
     * Configure message broker for pub/sub messaging
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple message broker for /topic destinations
        registry.enableSimpleBroker("/topic");

        // Prefix for message destinations to be sent to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }
}
