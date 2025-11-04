package com.courtvision.controller;

import com.courtvision.dto.ApiResponse;
import com.courtvision.dto.ScoreCalculationDTO;
import com.courtvision.entity.ScoreCalculation;
import com.courtvision.entity.User;
import com.courtvision.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for league scores
 * Provides endpoints for retrieving calculated league scores
 */
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /**
     * Get league scoreboard (all scores for a league)
     * @param leagueId The league ID
     * @return List of scores ordered by ranking
     */
    @GetMapping("/leagues/{leagueId}/scoreboard")
    public ResponseEntity<ApiResponse> getLeagueScoreboard(@PathVariable Long leagueId) {
        List<ScoreCalculation> scores = scoreService.getLeagueScoreboard(leagueId);

        List<ScoreCalculationDTO> dtos = scores.stream()
                .map(ScoreCalculationDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Retrieved " + dtos.size() + " scores for league")
                        .data(dtos)
                        .build()
        );
    }

    /**
     * Get user's score in a league
     * @param leagueId The league ID
     * @param userId The user ID
     * @return User's latest score
     */
    @GetMapping("/leagues/{leagueId}/users/{userId}")
    public ResponseEntity<ApiResponse> getUserScore(
            @PathVariable Long leagueId,
            @PathVariable Long userId) {

        Optional<ScoreCalculation> score = scoreService.getUserScore(leagueId, userId);

        if (score.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Retrieved user score")
                            .data(ScoreCalculationDTO.fromEntity(score.get()))
                            .build()
            );
        } else {
            return ResponseEntity.status(404).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("No score found for user in this league")
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Get current user's score in a league
     * @param leagueId The league ID
     * @param authentication Spring Security authentication
     * @return User's latest score
     */
    @GetMapping("/leagues/{leagueId}/me")
    public ResponseEntity<ApiResponse> getMyScore(
            @PathVariable Long leagueId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        Optional<ScoreCalculation> score = scoreService.getUserScore(leagueId, user.getId());

        if (score.isPresent()) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Retrieved your score")
                            .data(ScoreCalculationDTO.fromEntity(score.get()))
                            .build()
            );
        } else {
            return ResponseEntity.status(404).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("No score found for you in this league")
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Trigger manual score recalculation for a league
     * Only league creator can trigger this
     * @param leagueId The league ID
     * @param authentication Spring Security authentication
     * @return Recalculated scores
     */
    @PostMapping("/leagues/{leagueId}/recalculate")
    public ResponseEntity<ApiResponse> recalculateScores(
            @PathVariable Long leagueId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        // In a real app, verify user is league creator
        // For now, allowing anyone to trigger

        try {
            scoreService.calculateLeagueScores(leagueId);

            List<ScoreCalculation> scores = scoreService.getLeagueScoreboard(leagueId);
            List<ScoreCalculationDTO> dtos = scores.stream()
                    .map(ScoreCalculationDTO::fromEntity)
                    .toList();

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Scores recalculated successfully for league")
                            .data(dtos)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Error recalculating scores: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}
