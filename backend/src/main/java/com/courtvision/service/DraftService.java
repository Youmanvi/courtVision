package com.courtvision.service;

import com.courtvision.dto.*;
import com.courtvision.entity.*;
import com.courtvision.repository.DraftPickRepository;
import com.courtvision.repository.DraftRepository;
import com.courtvision.repository.LeagueMemberRepository;
import com.courtvision.repository.LeagueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing league drafts
 * Handles draft creation, initialization, pick management, and status updates
 */
@Service
@Slf4j
public class DraftService {

    private final DraftRepository draftRepository;
    private final DraftPickRepository draftPickRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueMemberRepository leagueMemberRepository;
    private final NBAPlayerService nbaPlayerService;

    public DraftService(DraftRepository draftRepository,
                       DraftPickRepository draftPickRepository,
                       LeagueRepository leagueRepository,
                       LeagueMemberRepository leagueMemberRepository,
                       NBAPlayerService nbaPlayerService) {
        this.draftRepository = draftRepository;
        this.draftPickRepository = draftPickRepository;
        this.leagueRepository = leagueRepository;
        this.leagueMemberRepository = leagueMemberRepository;
        this.nbaPlayerService = nbaPlayerService;
    }

    /**
     * Start a draft for a league
     * @param leagueId The league ID
     * @param request The start draft request with rounds per team
     * @param creator The user starting the draft (must be league creator)
     * @return DraftResponse for the created draft
     * @throws IllegalArgumentException if league not found or invalid state
     * @throws SecurityException if user is not league creator
     */
    @Transactional
    public DraftResponse startDraft(Long leagueId, StartDraftRequest request, User creator) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found with ID: " + leagueId));

        // Verify user is league creator
        if (!league.isCreator(creator)) {
            throw new SecurityException("Only league creator can start a draft");
        }

        // Check if draft already exists for this league
        if (draftRepository.existsByLeagueId(leagueId)) {
            throw new IllegalArgumentException("Draft already exists for this league");
        }

        // Set defaults
        request.setDefaults();

        // Validate rounds per team
        if (request.getRoundsPerTeam() == null || request.getRoundsPerTeam() < 1 || request.getRoundsPerTeam() > 20) {
            throw new IllegalArgumentException("Rounds per team must be between 1 and 20");
        }

        // Get league members
        List<LeagueMember> members = leagueMemberRepository.findByLeagueId(leagueId);
        if (members.isEmpty()) {
            throw new IllegalArgumentException("Cannot start draft with no league members");
        }

        int numTeams = members.size();
        int totalPicks = numTeams * request.getRoundsPerTeam();

        // Create draft
        Draft draft = Draft.builder()
                .league(league)
                .status(Draft.DraftStatus.SCHEDULED)
                .currentRound(1)
                .roundsPerTeam(request.getRoundsPerTeam())
                .currentPickOrder(1)
                .totalPicks(totalPicks)
                .picksMade(0)
                .currentPicker(members.get(0).getUser()) // First team starts
                .build();

        draft = draftRepository.save(draft);
        log.info("Draft started for league {} ({}) with {} teams and {} total picks",
                league.getId(), league.getName(), numTeams, totalPicks);

        return DraftResponse.fromEntity(draft);
    }

    /**
     * Get draft for a league
     * @param leagueId The league ID
     * @return DraftResponse if draft exists
     * @throws IllegalArgumentException if draft not found
     */
    @Transactional(readOnly = true)
    public DraftResponse getDraft(Long leagueId) {
        Draft draft = draftRepository.findByLeagueIdWithLeague(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found for league ID: " + leagueId));

        return DraftResponse.fromEntity(draft);
    }

    /**
     * Make a draft pick
     * @param draftId The draft ID
     * @param request The pick request (player name, etc)
     * @param picker The user making the pick (must be current picker)
     * @return DraftPickResponse for the created pick
     * @throws IllegalArgumentException if draft not found or invalid state
     * @throws SecurityException if user is not current picker
     */
    @Transactional
    public DraftPickResponse makePick(Long draftId, MakeDraftPickRequest request, User picker) {
        Draft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found with ID: " + draftId));

        // Verify draft is active
        if (!draft.isActive()) {
            throw new IllegalArgumentException("Draft is not active. Current status: " + draft.getStatus());
        }

        // Verify user is current picker
        if (!draft.getCurrentPicker().getId().equals(picker.getId())) {
            throw new SecurityException("Only the current picker can make a pick");
        }

        // Validate player name
        if (request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Player name is required");
        }

        // Validate player exists in NBA API (optional - only if API is available)
        try {
            if (!nbaPlayerService.playerExists(request.getPlayerName())) {
                log.warn("Player not found in NBA API: {}", request.getPlayerName());
                // Allow the pick anyway - player might be in API but not found due to API issues
                // This is a warning, not an error
            }
        } catch (Exception e) {
            log.warn("Error validating player with NBA API: {}", e.getMessage());
            // Don't fail the pick if API is unavailable - proceed anyway
        }

        // Check if player already drafted
        if (draftPickRepository.existsByDraftIdAndPlayerName(draftId, request.getPlayerName())) {
            throw new IllegalArgumentException("Player already drafted: " + request.getPlayerName());
        }

        // Create draft pick
        DraftPick pick = DraftPick.builder()
                .draft(draft)
                .picker(picker)
                .playerName(request.getPlayerName())
                .nbaPlayerId(request.getNbaPlayerId())
                .playerPosition(request.getPlayerPosition())
                .roundNumber(draft.getCurrentRound())
                .pickNumber(draft.getCurrentPickOrder())
                .build();

        pick = draftPickRepository.save(pick);

        // Update draft state
        draft.setPicksMade(draft.getPicksMade() + 1);

        // Check if draft is complete
        if (draft.isLastPick()) {
            draft.setStatus(Draft.DraftStatus.COMPLETED);
            draft.setDraftEndedAt(LocalDateTime.now());
            log.info("Draft {} completed with {} picks", draftId, draft.getPicksMade());
        } else {
            // Move to next picker
            moveToNextPicker(draft);
        }

        draftRepository.save(draft);
        log.info("Pick made by {} in draft {}: {}", picker.getUsername(), draftId, request.getPlayerName());

        return DraftPickResponse.fromEntity(pick);
    }

    /**
     * Get all picks for a draft
     * @param draftId The draft ID
     * @return List of DraftPickResponse objects
     */
    @Transactional(readOnly = true)
    public List<DraftPickResponse> getDraftPicks(Long draftId) {
        return draftPickRepository.findByDraftIdOrdered(draftId).stream()
                .map(DraftPickResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get picks made by a specific user in a draft
     * @param draftId The draft ID
     * @param userId The user ID
     * @return List of DraftPickResponse objects
     */
    @Transactional(readOnly = true)
    public List<DraftPickResponse> getUserDraftPicks(Long draftId, Long userId) {
        return draftPickRepository.findByDraftIdAndPickerId(draftId, userId).stream()
                .map(DraftPickResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Move to the next picker in draft order
     * Handles snake draft pattern (alternating direction each round)
     */
    private void moveToNextPicker(Draft draft) {
        int numTeams = draft.getTotalPicks() / draft.getRoundsPerTeam();

        // Determine if we're moving to next round
        if (draft.getCurrentPickOrder() >= numTeams) {
            // Move to next round (snake draft pattern)
            draft.setCurrentRound(draft.getCurrentRound() + 1);
            // In even rounds, picks go in reverse order
            if (draft.getCurrentRound() % 2 == 0) {
                draft.setCurrentPickOrder(numTeams);
            } else {
                draft.setCurrentPickOrder(1);
            }
        } else {
            // Continue in current round
            if (draft.getCurrentRound() % 2 == 0) {
                // Even round: descending order
                draft.setCurrentPickOrder(draft.getCurrentPickOrder() - 1);
            } else {
                // Odd round: ascending order
                draft.setCurrentPickOrder(draft.getCurrentPickOrder() + 1);
            }
        }

        // Get the next picker's team order
        List<LeagueMember> members = leagueMemberRepository.findByLeagueId(draft.getLeague().getId());
        if (!members.isEmpty()) {
            User nextPicker = members.get(draft.getCurrentPickOrder() - 1).getUser();
            draft.setCurrentPicker(nextPicker);
        }
    }

    /**
     * Pause the draft
     * @param draftId The draft ID
     * @param user The user pausing the draft (must be league creator)
     * @return DraftResponse for the paused draft
     */
    @Transactional
    public DraftResponse pauseDraft(Long draftId, User user) {
        Draft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found with ID: " + draftId));

        // Verify user is league creator
        if (!draft.getLeague().isCreator(user)) {
            throw new SecurityException("Only league creator can pause a draft");
        }

        if (!draft.isActive()) {
            throw new IllegalArgumentException("Can only pause active drafts");
        }

        draft.setStatus(Draft.DraftStatus.PAUSED);
        draft = draftRepository.save(draft);
        log.info("Draft {} paused", draftId);

        return DraftResponse.fromEntity(draft);
    }

    /**
     * Resume a paused draft
     * @param draftId The draft ID
     * @param user The user resuming the draft (must be league creator)
     * @return DraftResponse for the resumed draft
     */
    @Transactional
    public DraftResponse resumeDraft(Long draftId, User user) {
        Draft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found with ID: " + draftId));

        // Verify user is league creator
        if (!draft.getLeague().isCreator(user)) {
            throw new SecurityException("Only league creator can resume a draft");
        }

        if (draft.getStatus() != Draft.DraftStatus.PAUSED) {
            throw new IllegalArgumentException("Can only resume paused drafts");
        }

        draft.setStatus(Draft.DraftStatus.ACTIVE);
        draft = draftRepository.save(draft);
        log.info("Draft {} resumed", draftId);

        return DraftResponse.fromEntity(draft);
    }
}
