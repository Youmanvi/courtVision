package com.courtvision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a calculated score for a user's team in a league
 * Scores are calculated once daily based on player stats from NBA API
 */
@Entity
@Table(name = "score_calculations", indexes = {
    @Index(name = "idx_league_user", columnList = "league_id, user_id"),
    @Index(name = "idx_calculated_at", columnList = "calculated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double totalScore;

    @Column(nullable = false)
    private Integer playersEvaluated;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }

    /**
     * Get average score per player
     */
    public Double getAverageScore() {
        if (playersEvaluated == null || playersEvaluated == 0) {
            return 0.0;
        }
        return totalScore / playersEvaluated;
    }
}
