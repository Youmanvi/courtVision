package com.courtvision.config;

import com.courtvision.repository.UserRepository;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test-specific Security Configuration to avoid AuthenticationManager circular proxy issues
 * This configuration explicitly provides an AuthenticationManager for testing
 * Also imports TestKafkaConfig to mock Kafka dependencies
 */
@TestConfiguration
@Import(TestKafkaConfig.class)
public class TestSecurityConfig {

    /**
     * Provide a test-specific AuthenticationManager that doesn't create circular proxies
     */
    @Bean
    @Primary
    public AuthenticationManager testAuthenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    /**
     * Provide UserDetailsService for testing
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found: " + username));
    }
}
