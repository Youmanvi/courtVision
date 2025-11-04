package com.courtvision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to start a draft for a league
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartDraftRequest {

    /**
     * Number of rounds per team (default 3 for 3-round draft)
     */
    private Integer roundsPerTeam;

    // @Data already generates getters/setters via Lombok

    /**
     * Set default rounds if not provided
     */
    public void setDefaults() {
        if (roundsPerTeam == null) {
            roundsPerTeam = 3;
        }
    }
}
