package com.courtvision.dto;

import com.courtvision.entity.League;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueResponse {
    private Long id;
    private String name;
    private String description;
    private String creatorUsername;
    private Long creatorId;
    private Integer maxPlayers;
    private Integer currentMemberCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert League entity to response DTO
     */
    public static LeagueResponse fromEntity(League league, Integer memberCount) {
        return LeagueResponse.builder()
                .id(league.getId())
                .name(league.getName())
                .description(league.getDescription())
                .creatorUsername(league.getCreator().getUsername())
                .creatorId(league.getCreator().getId())
                .maxPlayers(league.getMaxPlayers())
                .currentMemberCount(memberCount)
                .status(league.getStatus().name())
                .createdAt(league.getCreatedAt())
                .updatedAt(league.getUpdatedAt())
                .build();
    }
}
