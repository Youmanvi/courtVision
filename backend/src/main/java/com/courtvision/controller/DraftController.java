package com.courtvision.controller;

import com.courtvision.dto.*;
import com.courtvision.entity.User;
import com.courtvision.service.DraftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing league drafts
 * Endpoints for starting drafts, making picks, and viewing draft status
 */
@RestController
@RequestMapping("/api/drafts")
@Slf4j
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    /**
     * Start a draft for a league
     * POST /api/drafts/leagues/{leagueId}/start
     *
     * @param leagueId The league ID
     * @param request The start draft request
     * @param user The authenticated user (must be league creator)
     * @return 201 CREATED with DraftResponse
     */
    @PostMapping("/leagues/{leagueId}/start")
    public ResponseEntity<ApiResponse> startDraft(
            @PathVariable Long leagueId,
            @RequestBody StartDraftRequest request,
            @AuthenticationPrincipal User user) {
        try {
            DraftResponse draft = draftService.startDraft(leagueId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Draft started successfully")
                            .data(draft)
                            .build());
        } catch (SecurityException e) {
            log.warn("Unauthorized draft start attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid draft start request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error starting draft", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error starting draft")
                            .build());
        }
    }

    /**
     * Get draft information for a league
     * GET /api/drafts/leagues/{leagueId}
     *
     * @param leagueId The league ID
     * @return 200 OK with DraftResponse
     */
    @GetMapping("/leagues/{leagueId}")
    public ResponseEntity<ApiResponse> getDraft(@PathVariable Long leagueId) {
        try {
            DraftResponse draft = draftService.getDraft(leagueId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Draft retrieved successfully")
                    .data(draft)
                    .build());
        } catch (IllegalArgumentException e) {
            log.warn("Draft not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error retrieving draft", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error retrieving draft")
                            .build());
        }
    }

    /**
     * Make a draft pick
     * POST /api/drafts/{draftId}/pick
     *
     * @param draftId The draft ID
     * @param request The draft pick request (player name, etc)
     * @param user The authenticated user (must be current picker)
     * @return 201 CREATED with DraftPickResponse
     */
    @PostMapping("/{draftId}/pick")
    public ResponseEntity<ApiResponse> makePick(
            @PathVariable Long draftId,
            @RequestBody MakeDraftPickRequest request,
            @AuthenticationPrincipal User user) {
        try {
            DraftPickResponse pick = draftService.makePick(draftId, request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Pick made successfully")
                            .data(pick)
                            .build());
        } catch (SecurityException e) {
            log.warn("Unauthorized pick attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid pick request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error making pick", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error making pick")
                            .build());
        }
    }

    /**
     * Get all picks in a draft
     * GET /api/drafts/{draftId}/picks
     *
     * @param draftId The draft ID
     * @return 200 OK with List of DraftPickResponse
     */
    @GetMapping("/{draftId}/picks")
    public ResponseEntity<ApiResponse> getDraftPicks(@PathVariable Long draftId) {
        try {
            List<DraftPickResponse> picks = draftService.getDraftPicks(draftId);
            return ResponseEntity.ok(ApiResponse.<List<DraftPickResponse>>builder()
                    .success(true)
                    .message("Draft picks retrieved successfully")
                    .data(picks)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving draft picks", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<List<DraftPickResponse>>builder()
                            .success(false)
                            .message("Error retrieving draft picks")
                            .build());
        }
    }

    /**
     * Get picks made by a specific user in a draft
     * GET /api/drafts/{draftId}/picks/user/{userId}
     *
     * @param draftId The draft ID
     * @param userId The user ID
     * @return 200 OK with List of DraftPickResponse
     */
    @GetMapping("/{draftId}/picks/user/{userId}")
    public ResponseEntity<ApiResponse> getUserDraftPicks(
            @PathVariable Long draftId,
            @PathVariable Long userId) {
        try {
            List<DraftPickResponse> picks = draftService.getUserDraftPicks(draftId, userId);
            return ResponseEntity.ok(ApiResponse.<List<DraftPickResponse>>builder()
                    .success(true)
                    .message("User draft picks retrieved successfully")
                    .data(picks)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving user draft picks", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<List<DraftPickResponse>>builder()
                            .success(false)
                            .message("Error retrieving user draft picks")
                            .build());
        }
    }

    /**
     * Pause a draft
     * PUT /api/drafts/{draftId}/pause
     *
     * @param draftId The draft ID
     * @param user The authenticated user (must be league creator)
     * @return 200 OK with DraftResponse
     */
    @PutMapping("/{draftId}/pause")
    public ResponseEntity<ApiResponse> pauseDraft(
            @PathVariable Long draftId,
            @AuthenticationPrincipal User user) {
        try {
            DraftResponse draft = draftService.pauseDraft(draftId, user);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Draft paused successfully")
                    .data(draft)
                    .build());
        } catch (SecurityException e) {
            log.warn("Unauthorized pause attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid pause request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error pausing draft", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error pausing draft")
                            .build());
        }
    }

    /**
     * Resume a paused draft
     * PUT /api/drafts/{draftId}/resume
     *
     * @param draftId The draft ID
     * @param user The authenticated user (must be league creator)
     * @return 200 OK with DraftResponse
     */
    @PutMapping("/{draftId}/resume")
    public ResponseEntity<ApiResponse> resumeDraft(
            @PathVariable Long draftId,
            @AuthenticationPrincipal User user) {
        try {
            DraftResponse draft = draftService.resumeDraft(draftId, user);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Draft resumed successfully")
                    .data(draft)
                    .build());
        } catch (SecurityException e) {
            log.warn("Unauthorized resume attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid resume request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error resuming draft", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Error resuming draft")
                            .build());
        }
    }
}
