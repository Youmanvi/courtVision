package com.courtvision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to make a draft pick
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MakeDraftPickRequest {

    /**
     * NBA player name
     */
    private String playerName;

    /**
     * NBA player ID (optional)
     */
    private String nbaPlayerId;

    /**
     * Player position (optional: PG, SG, SF, PF, C)
     */
    private String playerPosition;
}
