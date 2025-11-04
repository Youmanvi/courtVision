package com.courtvision.controller;

import com.courtvision.dto.*;
import com.courtvision.entity.User;
import com.courtvision.service.LeagueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@Slf4j
public class LeagueController {

    @Autowired
    private LeagueService leagueService;

    /**
     * Create a new league
     * POST /api/leagues
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createLeague(@RequestBody CreateLeagueRequest request) {
        try {
            User user = getCurrentUser();

            LeagueResponse response = leagueService.createLeague(request, user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("League created successfully")
                            .data(response)
                            .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error creating league: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error creating league: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get all leagues for the current user
     * GET /api/leagues
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getUserLeagues() {
        try {
            User user = getCurrentUser();

            List<LeagueResponse> leagues = leagueService.getUserLeagues(user);

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Leagues retrieved successfully")
                            .data(leagues)
                            .build());

        } catch (Exception e) {
            log.error("Error retrieving leagues: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error retrieving leagues: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get league details by ID
     * GET /api/leagues/{leagueId}
     */
    @GetMapping("/{leagueId}")
    public ResponseEntity<ApiResponse> getLeague(@PathVariable Long leagueId) {
        try {
            LeagueResponse league = leagueService.getLeagueById(leagueId);

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("League retrieved successfully")
                            .data(league)
                            .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error retrieving league: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error retrieving league: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Update league (creator only)
     * PUT /api/leagues/{leagueId}
     */
    @PutMapping("/{leagueId}")
    public ResponseEntity<ApiResponse> updateLeague(
            @PathVariable Long leagueId,
            @RequestBody CreateLeagueRequest request) {
        try {
            User user = getCurrentUser();

            LeagueResponse response = leagueService.updateLeague(leagueId, request, user);

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("League updated successfully")
                            .data(response)
                            .build());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating league: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error updating league: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Delete league (creator only)
     * DELETE /api/leagues/{leagueId}
     */
    @DeleteMapping("/{leagueId}")
    public ResponseEntity<ApiResponse> deleteLeague(@PathVariable Long leagueId) {
        try {
            User user = getCurrentUser();

            leagueService.deleteLeague(leagueId, user);

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("League deleted successfully")
                            .build());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting league: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error deleting league: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get league members
     * GET /api/leagues/{leagueId}/members
     */
    @GetMapping("/{leagueId}/members")
    public ResponseEntity<ApiResponse> getLeagueMembers(@PathVariable Long leagueId) {
        try {
            List<LeagueMemberResponse> members = leagueService.getLeagueMembers(leagueId);

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Members retrieved successfully")
                            .data(members)
                            .build());

        } catch (Exception e) {
            log.error("Error retrieving members: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error retrieving members: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Invite a player to the league (creator only)
     * POST /api/leagues/{leagueId}/invite
     */
    @PostMapping("/{leagueId}/invite")
    public ResponseEntity<ApiResponse> invitePlayer(
            @PathVariable Long leagueId,
            @RequestBody InvitePlayerRequest request) {
        try {
            User user = getCurrentUser();

            InvitationResponse response = leagueService.invitePlayer(leagueId, request, user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Invitation sent successfully")
                            .data(response)
                            .build());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error inviting player: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error inviting player: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Join a league using invitation token
     * POST /api/leagues/join?token={token}
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse> joinLeague(@RequestParam String token) {
        try {
            User user = getCurrentUser();

            LeagueMemberResponse response = leagueService.joinLeague(token, user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Joined league successfully")
                            .data(response)
                            .build());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error joining league: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error joining league: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get pending invitations for current user
     * GET /api/leagues/invitations/pending
     */
    @GetMapping("/invitations/pending")
    public ResponseEntity<ApiResponse> getPendingInvitations() {
        try {
            User user = getCurrentUser();

            List<InvitationResponse> invitations = leagueService.getPendingInvitations(user);

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Pending invitations retrieved successfully")
                            .data(invitations)
                            .build());

        } catch (Exception e) {
            log.error("Error retrieving invitations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error retrieving invitations: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get the authenticated user from SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}
