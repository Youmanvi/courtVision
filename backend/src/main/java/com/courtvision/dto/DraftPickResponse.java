package com.courtvision.dto;

import com.courtvision.entity.DraftPick;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for DraftPick entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftPickResponse {

    private Long id;
    private Long draftId;
    private Long pickerId;
    private String pickerUsername;
    private String playerName;
    private String nbaPlayerId;
    private String playerPosition;
    private Integer roundNumber;
    private Integer pickNumber;
    private Integer overallPickNumber;
    private LocalDateTime pickedAt;
    private LocalDateTime createdAt;

    /**
     * Convert DraftPick entity to response DTO
     */
    public static DraftPickResponse fromEntity(DraftPick pick) {
        return DraftPickResponse.builder()
                .id(pick.getId())
                .draftId(pick.getDraft().getId())
                .pickerId(pick.getPicker().getId())
                .pickerUsername(pick.getPicker().getUsername())
                .playerName(pick.getPlayerName())
                .nbaPlayerId(pick.getNbaPlayerId())
                .playerPosition(pick.getPlayerPosition())
                .roundNumber(pick.getRoundNumber())
                .pickNumber(pick.getPickNumber())
                .overallPickNumber(pick.getOverallPickNumber())
                .pickedAt(pick.getPickedAt())
                .createdAt(pick.getCreatedAt())
                .build();
    }
}
