package com.courtvision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Kafka event message for score updates
 * Published when scores are calculated for a league
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreUpdateEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("league_id")
    private Long leagueId;

    @JsonProperty("league_name")
    private String leagueName;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("total_score")
    private Double totalScore;

    @JsonProperty("average_score")
    private Double averageScore;

    @JsonProperty("players_evaluated")
    private Integer playersEvaluated;

    @JsonProperty("rank")
    private Integer rank;

    @JsonProperty("calculated_at")
    private LocalDateTime calculatedAt;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("event_type")
    private String eventType; // "LEAGUE_SCORES_UPDATED"
}
