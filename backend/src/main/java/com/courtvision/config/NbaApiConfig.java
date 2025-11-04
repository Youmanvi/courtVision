package com.courtvision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for NBA/SportsBlaze API integration
 */
@Configuration
@ConfigurationProperties(prefix = "nba.api")
@Data
public class NbaApiConfig {

    private String baseUrl;
    private String key;
    private long cacheTtl;
    private int timeout;

}
