package com.courtvision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for NBA player data from SportsBlaze API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NBAPlayerDTO {

    @JsonProperty("player_id")
    private String playerId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("position")
    private String position;

    @JsonProperty("team")
    private String team;

    @JsonProperty("team_id")
    private String teamId;

    @JsonProperty("height")
    private String height;

    @JsonProperty("weight")
    private String weight;

    @JsonProperty("college")
    private String college;

    @JsonProperty("salary")
    private Double salary;

    @JsonProperty("jersey_number")
    private Integer jerseyNumber;

    /**
     * Get full player name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if player is available (not injured, etc)
     */
    public boolean isAvailable() {
        // Can be extended with injury status from API
        return true;
    }
}
