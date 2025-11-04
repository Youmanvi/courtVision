package com.courtvision.repository;

import com.courtvision.entity.LeagueInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueInvitationRepository extends JpaRepository<LeagueInvitation, Long> {

    /**
     * Find invitation by token
     */
    Optional<LeagueInvitation> findByInvitationToken(String invitationToken);

    /**
     * Find all invitations for a specific league
     */
    List<LeagueInvitation> findByLeagueId(Long leagueId);

    /**
     * Find pending invitations for a league
     */
    List<LeagueInvitation> findByLeagueIdAndStatus(Long leagueId, LeagueInvitation.InvitationStatus status);

    /**
     * Find invitations for a specific email address
     */
    List<LeagueInvitation> findByInvitedEmail(String email);

    /**
     * Find pending invitations for an email
     */
    @Query("SELECT li FROM LeagueInvitation li " +
           "WHERE li.invitedEmail = :email " +
           "AND li.status = 'PENDING' " +
           "AND li.expiresAt > CURRENT_TIMESTAMP")
    List<LeagueInvitation> findPendingByEmail(@Param("email") String email);

    /**
     * Check if an invitation already exists for email in a league
     */
    boolean existsByLeagueIdAndInvitedEmailAndStatus(Long leagueId, String email, LeagueInvitation.InvitationStatus status);

    /**
     * Find invitations by user (registered user who was invited)
     */
    List<LeagueInvitation> findByInvitedUserId(Long userId);
}
