package com.courtvision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DraftPick entity - represents a single pick/selection in a draft
 * Tracks which player was selected by whom and when
 */
@Entity
@Table(name = "draft_picks", indexes = {
    @Index(name = "idx_draft_id", columnList = "draft_id"),
    @Index(name = "idx_picker_id", columnList = "picker_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id")
    private Draft draft;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "picker_id")
    private User picker;

    @Column(name = "player_name", nullable = false, length = 255)
    private String playerName;

    @Column(name = "nba_player_id", length = 255)
    private String nbaPlayerId;

    @Column(name = "player_position", length = 50)
    private String playerPosition;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "pick_number", nullable = false)
    private Integer pickNumber;

    @Column(name = "picked_at", nullable = false)
    private LocalDateTime pickedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        pickedAt = LocalDateTime.now();
    }

    /**
     * Get overall pick number across all rounds
     */
    public Integer getOverallPickNumber() {
        return (roundNumber - 1) * 10 + pickNumber;
    }
}
