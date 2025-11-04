package com.courtvision.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for RestTemplate bean
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, NbaApiConfig nbaApiConfig) {
        Duration timeout = Duration.ofMillis(nbaApiConfig.getTimeout());
        return builder
                .requestFactory(() -> {
                    org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                        new org.springframework.http.client.SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) timeout.toMillis());
                    factory.setReadTimeout((int) timeout.toMillis());
                    return factory;
                })
                .build();
    }
}
