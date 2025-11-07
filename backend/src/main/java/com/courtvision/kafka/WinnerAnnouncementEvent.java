package com.courtvision.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Winner Announcement Event
 * Published when a league winner is announced and needs Solana blockchain submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WinnerAnnouncementEvent {

    /**
     * Unique event ID
     */
    @JsonProperty("event_id")
    private String eventId;

    /**
     * Event type (should be "LEAGUE_WINNER_ANNOUNCED")
     */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * League ID
     */
    @JsonProperty("league_id")
    private Long leagueId;

    /**
     * League name
     */
    @JsonProperty("league_name")
    private String leagueName;

    /**
     * Winner user ID
     */
    @JsonProperty("winner_id")
    private Long winnerId;

    /**
     * Winner username
     */
    @JsonProperty("winner_username")
    private String winnerUsername;

    /**
     * Winner's Solana wallet address
     */
    @JsonProperty("solana_wallet")
    private String solanaWallet;

    /**
     * Final score that won the league
     */
    @JsonProperty("final_score")
    private Double finalScore;

    /**
     * Winner's rank (1 for first place)
     */
    @JsonProperty("rank")
    private Integer rank;

    /**
     * Total number of participants
     */
    @JsonProperty("total_participants")
    private Integer totalParticipants;

    /**
     * Timestamp when winner was announced
     */
    @JsonProperty("announced_at")
    private LocalDateTime announcedAt;

    /**
     * Solana transaction hash after blockchain submission
     */
    @JsonProperty("solana_transaction_hash")
    private String solanaTransactionHash;

    /**
     * Transaction status
     */
    @JsonProperty("transaction_status")
    private String transactionStatus;

    /**
     * Network (mainnet or devnet)
     */
    @JsonProperty("network")
    private String network;

    /**
     * Error message if submission failed
     */
    @JsonProperty("error_message")
    private String errorMessage;
}
