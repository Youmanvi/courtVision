package com.courtvision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Draft entity - represents a draft event for a league
 * Manages the draft process including turn order, picks, and status
 */
@Entity
@Table(name = "drafts", indexes = {
    @Index(name = "idx_league_id", columnList = "league_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id")
    private League league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftStatus status;

    @Column(nullable = false)
    private Integer currentRound;

    @Column(nullable = false)
    private Integer roundsPerTeam;

    @Column(name = "current_pick_order", nullable = false)
    private Integer currentPickOrder;

    @Column(name = "total_picks", nullable = false)
    private Integer totalPicks;

    @Column(name = "picks_made", nullable = false)
    private Integer picksMade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_picker_id")
    private User currentPicker;

    @Column(name = "draft_started_at")
    private LocalDateTime draftStartedAt;

    @Column(name = "draft_ended_at")
    private LocalDateTime draftEndedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        picksMade = 0;
        status = DraftStatus.SCHEDULED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if draft is active
     */
    public boolean isActive() {
        return status == DraftStatus.ACTIVE;
    }

    /**
     * Check if draft is completed
     */
    public boolean isCompleted() {
        return status == DraftStatus.COMPLETED;
    }

    /**
     * Check if draft is scheduled but not started
     */
    public boolean isScheduled() {
        return status == DraftStatus.SCHEDULED;
    }

    /**
     * Check if current pick is the last pick
     */
    public boolean isLastPick() {
        return picksMade >= totalPicks - 1;
    }

    /**
     * Move to next round (snake draft pattern)
     */
    public void moveToNextRound() {
        currentRound++;
        // Snake draft: odd rounds go ascending, even rounds go descending
        if (currentRound % 2 == 0) {
            currentPickOrder--;
        } else {
            currentPickOrder++;
        }
    }

    /**
     * Draft status enum
     */
    public enum DraftStatus {
        SCHEDULED,  // Draft created but not started
        ACTIVE,     // Draft in progress
        PAUSED,     // Draft paused
        COMPLETED,  // Draft finished
        CANCELLED   // Draft cancelled
    }
}
