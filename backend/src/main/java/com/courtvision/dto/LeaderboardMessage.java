package com.courtvision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Message sent via WebSocket for leaderboard updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardMessage {
    @JsonProperty("league_id")
    private Long leagueId;

    @JsonProperty("league_name")
    private String leagueName;

    @JsonProperty("scores")
    private List<ScoreCalculationDTO> scores;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("message_type")
    private String messageType; // "SCORES_UPDATED", "INITIAL_LOAD", etc.
}
