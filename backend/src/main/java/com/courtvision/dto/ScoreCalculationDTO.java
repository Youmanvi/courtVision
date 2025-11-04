package com.courtvision.dto;

import com.courtvision.entity.ScoreCalculation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning score calculations via REST API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreCalculationDTO {

    @JsonProperty("id")
    private Long id;

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

    @JsonProperty("calculated_at")
    private LocalDateTime calculatedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * Convert ScoreCalculation entity to DTO
     */
    public static ScoreCalculationDTO fromEntity(ScoreCalculation score) {
        return ScoreCalculationDTO.builder()
                .id(score.getId())
                .leagueId(score.getLeague().getId())
                .leagueName(score.getLeague().getName())
                .userId(score.getUser().getId())
                .username(score.getUser().getUsername())
                .totalScore(score.getTotalScore())
                .averageScore(score.getAverageScore())
                .playersEvaluated(score.getPlayersEvaluated())
                .calculatedAt(score.getCalculatedAt())
                .createdAt(score.getCreatedAt())
                .build();
    }
}
