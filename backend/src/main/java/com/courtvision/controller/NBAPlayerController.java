package com.courtvision.controller;

import com.courtvision.dto.ApiResponse;
import com.courtvision.dto.NBAPlayerDTO;
import com.courtvision.service.NBAPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for NBA player data endpoints
 */
@RestController
@RequestMapping("/api/nba")
@RequiredArgsConstructor
public class NBAPlayerController {

    private final NBAPlayerService nbaPlayerService;

    /**
     * Get all available NBA players
     */
    @GetMapping("/players")
    public ResponseEntity<ApiResponse> getAllPlayers() {
        List<NBAPlayerDTO> players = nbaPlayerService.getAllPlayers();
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Retrieved " + players.size() + " players")
                        .data(players)
                        .build()
        );
    }

    /**
     * Search players by name
     */
    @GetMapping("/players/search")
    public ResponseEntity<ApiResponse> searchPlayersByName(
            @RequestParam(required = true) String name) {
        List<NBAPlayerDTO> players = nbaPlayerService.searchPlayersByName(name);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Found " + players.size() + " players matching '" + name + "'")
                        .data(players)
                        .build()
        );
    }

    /**
     * Search players by team
     */
    @GetMapping("/players/team")
    public ResponseEntity<ApiResponse> searchPlayersByTeam(
            @RequestParam(required = true) String team) {
        List<NBAPlayerDTO> players = nbaPlayerService.searchPlayersByTeam(team);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Found " + players.size() + " players on team '" + team + "'")
                        .data(players)
                        .build()
        );
    }

    /**
     * Search players by position
     */
    @GetMapping("/players/position")
    public ResponseEntity<ApiResponse> searchPlayersByPosition(
            @RequestParam(required = true) String position) {
        List<NBAPlayerDTO> players = nbaPlayerService.searchPlayersByPosition(position);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Found " + players.size() + " players at position '" + position + "'")
                        .data(players)
                        .build()
        );
    }

    /**
     * Get player by ID
     */
    @GetMapping("/players/{playerId}")
    public ResponseEntity<ApiResponse> getPlayerById(@PathVariable String playerId) {
        NBAPlayerDTO player = nbaPlayerService.getPlayerById(playerId);
        if (player != null) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Player found")
                            .data(player)
                            .build()
            );
        } else {
            return ResponseEntity.status(404).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Player not found with ID: " + playerId)
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Get all unique positions
     */
    @GetMapping("/positions")
    public ResponseEntity<ApiResponse> getAllPositions() {
        List<String> positions = nbaPlayerService.getAllPositions();
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Retrieved " + positions.size() + " unique positions")
                        .data(positions)
                        .build()
        );
    }

    /**
     * Get all unique teams
     */
    @GetMapping("/teams")
    public ResponseEntity<ApiResponse> getAllTeams() {
        List<String> teams = nbaPlayerService.getAllTeams();
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Retrieved " + teams.size() + " unique teams")
                        .data(teams)
                        .build()
        );
    }
}
