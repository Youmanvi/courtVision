package com.courtvision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "league_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(nullable = false, length = 255)
    private String invitedEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private User invitedUser;

    @Column(nullable = false, unique = true, length = 255)
    private String invitationToken;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if the invitation is still valid (not expired and pending)
     */
    public boolean isValid() {
        return status == InvitationStatus.PENDING &&
               expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if the invitation has expired
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Invitation status enum
     */
    public enum InvitationStatus {
        PENDING,    // Awaiting acceptance
        ACCEPTED,   // User has joined the league
        REJECTED,   // User declined
        EXPIRED,    // Invitation expired
        REVOKED     // Creator cancelled the invitation
    }
}
