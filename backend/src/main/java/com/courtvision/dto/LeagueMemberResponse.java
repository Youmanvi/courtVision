package com.courtvision.dto;

import com.courtvision.entity.LeagueMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueMemberResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private LocalDateTime joinedAt;

    /**
     * Convert LeagueMember entity to response DTO
     */
    public static LeagueMemberResponse fromEntity(LeagueMember member) {
        return LeagueMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
