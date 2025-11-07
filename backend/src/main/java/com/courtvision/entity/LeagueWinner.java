package com.courtvision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LeagueWinner Entity
 * Records final winners and their Solana wallet addresses for blockchain submission
 */
@Entity
@Table(name = "league_winners", indexes = {
    @Index(name = "idx_league_winner", columnList = "league_id"),
    @Index(name = "idx_winner_date", columnList = "announced_at"),
    @Index(name = "idx_transaction_hash", columnList = "solana_transaction_hash")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User winner;

    /**
     * Final score that won the league
     */
    @Column(name = "final_score", nullable = false)
    private Double finalScore;

    /**
     * Winner's rank in the league (1 for winner)
     */
    @Column(name = "rank", nullable = false)
    private Integer rank;

    /**
     * Winner's Solana wallet address
     */
    @Column(name = "solana_wallet", length = 44, nullable = false)
    private String solanaWallet;

    /**
     * Solana blockchain transaction hash after announcement
     */
    @Column(name = "solana_transaction_hash", length = 88)
    private String solanaTransactionHash;

    /**
     * Status of the Solana transaction
     */
    @Column(name = "transaction_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    /**
     * Timestamp when winner was announced
     */
    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt;

    /**
     * Timestamp when transaction was confirmed on Solana
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * Error message if transaction failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (announcedAt == null) {
            announcedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
