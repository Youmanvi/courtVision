package com.courtvision.service;

import com.courtvision.dto.*;
import com.courtvision.entity.*;
import com.courtvision.repository.LeagueInvitationRepository;
import com.courtvision.repository.LeagueMemberRepository;
import com.courtvision.repository.LeagueRepository;
import com.courtvision.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private LeagueMemberRepository leagueMemberRepository;

    @Autowired
    private LeagueInvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new league
     * Only the authenticated user (creator) can create
     */
    @Transactional
    public LeagueResponse createLeague(CreateLeagueRequest request, User creator) {
        // Validation
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("League name is required");
        }
        if (request.getName().length() < 3 || request.getName().length() > 100) {
            throw new IllegalArgumentException("League name must be between 3 and 100 characters");
        }

        request.setDefaults();

        if (request.getMaxPlayers() < 2 || request.getMaxPlayers() > 8) {
            throw new IllegalArgumentException("League size must be between 2 and 8 players");
        }

        // Check if league name already exists for this creator
        if (leagueRepository.existsByNameAndCreatorId(request.getName(), creator.getId())) {
            throw new IllegalArgumentException("You already have a league with this name");
        }

        // Create league
        League league = League.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creator(creator)
                .maxPlayers(request.getMaxPlayers())
                .status(League.LeagueStatus.ACTIVE)
                .build();

        league = leagueRepository.save(league);

        // Add creator as league member with OWNER role
        LeagueMember creatorMember = LeagueMember.builder()
                .league(league)
                .user(creator)
                .role(LeagueMember.MemberRole.OWNER)
                .build();

        leagueMemberRepository.save(creatorMember);

        log.info("League created successfully: {} (ID: {}) by user: {}",
                 league.getName(), league.getId(), creator.getUsername());

        return LeagueResponse.fromEntity(league, 1);
    }

    /**
     * Get all leagues for a user (created or joined)
     */
    @Transactional(readOnly = true)
    public List<LeagueResponse> getUserLeagues(User user) {
        List<League> leagues = leagueRepository.findAllLeaguesForUser(user.getId());

        return leagues.stream()
                .map(league -> {
                    long memberCount = leagueMemberRepository.countByLeagueId(league.getId());
                    return LeagueResponse.fromEntity(league, (int) memberCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get league details by ID
     */
    @Transactional(readOnly = true)
    public LeagueResponse getLeagueById(Long leagueId) {
        League league = leagueRepository.findByIdWithCreator(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        long memberCount = leagueMemberRepository.countByLeagueId(leagueId);

        return LeagueResponse.fromEntity(league, (int) memberCount);
    }

    /**
     * Update league (only creator can modify)
     */
    @Transactional
    public LeagueResponse updateLeague(Long leagueId, CreateLeagueRequest request, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        // Only creator can modify
        if (!league.isCreator(user)) {
            throw new SecurityException("Only the league creator can modify the league");
        }

        // Validation
        if (request.getName() != null && !request.getName().isEmpty()) {
            if (request.getName().length() < 3 || request.getName().length() > 100) {
                throw new IllegalArgumentException("League name must be between 3 and 100 characters");
            }
            league.setName(request.getName());
        }

        if (request.getDescription() != null) {
            league.setDescription(request.getDescription());
        }

        if (request.getMaxPlayers() != null) {
            if (request.getMaxPlayers() < 2 || request.getMaxPlayers() > 8) {
                throw new IllegalArgumentException("League size must be between 2 and 8 players");
            }
            long currentMembers = leagueMemberRepository.countByLeagueId(leagueId);
            if (request.getMaxPlayers() < currentMembers) {
                throw new IllegalArgumentException(
                        "Cannot reduce league size below current member count (" + currentMembers + ")");
            }
            league.setMaxPlayers(request.getMaxPlayers());
        }

        league = leagueRepository.save(league);

        long memberCount = leagueMemberRepository.countByLeagueId(leagueId);

        log.info("League updated: {} (ID: {})", league.getName(), league.getId());

        return LeagueResponse.fromEntity(league, (int) memberCount);
    }

    /**
     * Delete league (only creator can delete)
     */
    @Transactional
    public void deleteLeague(Long leagueId, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        // Only creator can delete
        if (!league.isCreator(user)) {
            throw new SecurityException("Only the league creator can delete the league");
        }

        // Soft delete by marking as DELETED
        league.setStatus(League.LeagueStatus.DELETED);
        leagueRepository.save(league);

        log.info("League deleted: {} (ID: {})", league.getName(), league.getId());
    }

    /**
     * Get league members
     */
    @Transactional(readOnly = true)
    public List<LeagueMemberResponse> getLeagueMembers(Long leagueId) {
        List<LeagueMember> members = leagueMemberRepository.findByLeagueId(leagueId);

        return members.stream()
                .map(LeagueMemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Invite a player to the league (only creator can invite)
     */
    @Transactional
    public InvitationResponse invitePlayer(Long leagueId, InvitePlayerRequest request, User creator) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        // Only creator can invite
        if (!league.isCreator(creator)) {
            throw new SecurityException("Only the league creator can invite players");
        }

        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if creator is trying to invite themselves
        if (request.getEmail().equalsIgnoreCase(creator.getEmail())) {
            throw new IllegalArgumentException("You cannot invite yourself");
        }

        // Check league is not full
        long memberCount = leagueMemberRepository.countByLeagueId(leagueId);
        if (memberCount >= league.getMaxPlayers()) {
            throw new IllegalArgumentException("League is full");
        }

        // Check if user is already a member
        User invitedUser = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (invitedUser != null &&
            leagueMemberRepository.existsByLeagueIdAndUserId(leagueId, invitedUser.getId())) {
            throw new IllegalArgumentException("User is already a member of this league");
        }

        // Check if invitation already exists
        if (invitationRepository.existsByLeagueIdAndInvitedEmailAndStatus(
                leagueId, request.getEmail(), LeagueInvitation.InvitationStatus.PENDING)) {
            throw new IllegalArgumentException("Invitation already sent to this email");
        }

        // Generate invitation token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        LeagueInvitation invitation = LeagueInvitation.builder()
                .league(league)
                .invitedEmail(request.getEmail())
                .invitedUser(invitedUser)
                .invitationToken(token)
                .expiresAt(expiresAt)
                .status(LeagueInvitation.InvitationStatus.PENDING)
                .build();

        invitation = invitationRepository.save(invitation);

        log.info("Invitation sent to {} for league {} (ID: {})",
                 request.getEmail(), league.getName(), league.getId());

        return InvitationResponse.fromEntity(invitation);
    }

    /**
     * Join league using invitation token
     */
    @Transactional
    public LeagueMemberResponse joinLeague(String token, User user) {
        LeagueInvitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        // Check if invitation is valid
        if (!invitation.isValid()) {
            if (invitation.isExpired()) {
                throw new IllegalArgumentException("Invitation has expired");
            }
            throw new IllegalArgumentException("Invitation is no longer valid");
        }

        // Check if email matches
        if (!user.getEmail().equalsIgnoreCase(invitation.getInvitedEmail())) {
            throw new SecurityException("Email does not match invitation");
        }

        // Check if already a member
        if (leagueMemberRepository.existsByLeagueIdAndUserId(
                invitation.getLeague().getId(), user.getId())) {
            throw new IllegalArgumentException("You are already a member of this league");
        }

        // Add user to league
        LeagueMember member = LeagueMember.builder()
                .league(invitation.getLeague())
                .user(user)
                .role(LeagueMember.MemberRole.MEMBER)
                .build();

        member = leagueMemberRepository.save(member);

        // Mark invitation as accepted
        invitation.setStatus(LeagueInvitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        log.info("User {} joined league {} (ID: {})",
                 user.getUsername(), invitation.getLeague().getName(), invitation.getLeague().getId());

        return LeagueMemberResponse.fromEntity(member);
    }

    /**
     * Get pending invitations for a user
     */
    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitations(User user) {
        List<LeagueInvitation> invitations = invitationRepository.findPendingByEmail(user.getEmail());

        return invitations.stream()
                .map(InvitationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Remove a member from the league (only creator can remove)
     */
    @Transactional
    public void removeMember(Long leagueId, Long userId, User requestingUser) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        // Only creator can remove members
        if (!league.isCreator(requestingUser)) {
            throw new SecurityException("Only the league creator can remove members");
        }

        // Validate user exists
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user is actually a member
        LeagueMember member = leagueMemberRepository.findByLeagueIdAndUserId(leagueId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this league"));

        // Cannot remove the creator (league owner)
        if (member.isOwner()) {
            throw new IllegalArgumentException("Cannot remove the league creator");
        }

        // Remove member
        leagueMemberRepository.delete(member);

        log.info("Member {} removed from league {} (ID: {})",
                 userToRemove.getUsername(), league.getName(), league.getId());
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null &&
               email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
