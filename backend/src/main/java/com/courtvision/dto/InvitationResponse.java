package com.courtvision.dto;

import com.courtvision.entity.LeagueInvitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {
    private Long id;
    private Long leagueId;
    private String leagueName;
    private String invitedEmail;
    private String invitationToken;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;

    /**
     * Convert LeagueInvitation entity to response DTO
     */
    public static InvitationResponse fromEntity(LeagueInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .leagueId(invitation.getLeague().getId())
                .leagueName(invitation.getLeague().getName())
                .invitedEmail(invitation.getInvitedEmail())
                .invitationToken(invitation.getInvitationToken().toString())
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .acceptedAt(invitation.getAcceptedAt())
                .build();
    }
}
