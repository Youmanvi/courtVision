package com.courtvision.dto;

import com.courtvision.entity.Draft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Draft entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftResponse {

    private Long id;
    private Long leagueId;
    private String leagueName;
    private String status;
    private Integer currentRound;
    private Integer roundsPerTeam;
    private Integer currentPickOrder;
    private Integer totalPicks;
    private Integer picksMade;
    private String currentPickerUsername;
    private Long currentPickerId;
    private LocalDateTime draftStartedAt;
    private LocalDateTime draftEndedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Draft entity to response DTO
     */
    public static DraftResponse fromEntity(Draft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .leagueId(draft.getLeague().getId())
                .leagueName(draft.getLeague().getName())
                .status(draft.getStatus().name())
                .currentRound(draft.getCurrentRound())
                .roundsPerTeam(draft.getRoundsPerTeam())
                .currentPickOrder(draft.getCurrentPickOrder())
                .totalPicks(draft.getTotalPicks())
                .picksMade(draft.getPicksMade())
                .currentPickerUsername(draft.getCurrentPicker() != null ? draft.getCurrentPicker().getUsername() : null)
                .currentPickerId(draft.getCurrentPicker() != null ? draft.getCurrentPicker().getId() : null)
                .draftStartedAt(draft.getDraftStartedAt())
                .draftEndedAt(draft.getDraftEndedAt())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}
