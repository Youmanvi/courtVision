package com.courtvision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "league_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if this member is an owner
     */
    public boolean isOwner() {
        return role == MemberRole.OWNER;
    }

    /**
     * Member role enum
     */
    public enum MemberRole {
        OWNER,      // League creator (can manage)
        MEMBER      // Regular member (can view/play)
    }
}
