package com.courtvision.service;

import com.courtvision.config.NbaApiConfig;
import com.courtvision.dto.NBAPlayerDTO;
import com.courtvision.dto.NBAPlayersResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for fetching and managing NBA player data from SportsBlaze API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NBAPlayerService {

    private final RestTemplate restTemplate;
    private final NbaApiConfig nbaApiConfig;

    /**
     * Fetch all players from SportsBlaze API
     * Results are cached for 1 hour
     */
    @Cacheable("nbaPlayers")
    public List<NBAPlayerDTO> getAllPlayers() {
        try {
            log.info("Fetching all NBA players from SportsBlaze API");
            String url = buildPlayerUrl();
            NBAPlayersResponseDTO response = restTemplate.getForObject(url, NBAPlayersResponseDTO.class);

            if (response != null && response.getSuccess() && response.getPlayers() != null) {
                log.info("Successfully fetched {} players from API", response.getPlayers().size());
                return response.getPlayers();
            } else {
                log.warn("API returned unsuccessful response: {}", response != null ? response.getMessage() : "null response");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error fetching NBA players from API", e);
            return new ArrayList<>();
        }
    }

    /**
     * Search players by name (case-insensitive)
     */
    public List<NBAPlayerDTO> searchPlayersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = name.toLowerCase();
        return getAllPlayers().stream()
                .filter(player -> player.getFirstName() != null && player.getLastName() != null)
                .filter(player ->
                    player.getFirstName().toLowerCase().contains(searchTerm) ||
                    player.getLastName().toLowerCase().contains(searchTerm) ||
                    (player.getFirstName() + " " + player.getLastName()).toLowerCase().contains(searchTerm)
                )
                .collect(Collectors.toList());
    }

    /**
     * Search players by team
     */
    public List<NBAPlayerDTO> searchPlayersByTeam(String team) {
        if (team == null || team.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return getAllPlayers().stream()
                .filter(player -> player.getTeam() != null &&
                        player.getTeam().equalsIgnoreCase(team))
                .collect(Collectors.toList());
    }

    /**
     * Search players by position
     */
    public List<NBAPlayerDTO> searchPlayersByPosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return getAllPlayers().stream()
                .filter(player -> player.getPosition() != null &&
                        player.getPosition().equalsIgnoreCase(position))
                .collect(Collectors.toList());
    }

    /**
     * Get player by ID
     */
    public NBAPlayerDTO getPlayerById(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return null;
        }

        return getAllPlayers().stream()
                .filter(player -> player.getPlayerId() != null &&
                        player.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if player exists by name
     */
    public boolean playerExists(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }

        return !searchPlayersByName(playerName).isEmpty();
    }

    /**
     * Get all unique positions
     */
    public List<String> getAllPositions() {
        return getAllPlayers().stream()
                .map(NBAPlayerDTO::getPosition)
                .distinct()
                .filter(position -> position != null && !position.isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all unique teams
     */
    public List<String> getAllTeams() {
        return getAllPlayers().stream()
                .map(NBAPlayerDTO::getTeam)
                .distinct()
                .filter(team -> team != null && !team.isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Build the API URL with authentication key
     */
    private String buildPlayerUrl() {
        return UriComponentsBuilder.fromHttpUrl(nbaApiConfig.getBaseUrl())
                .path("/splits/players/2025/regular_season.json")
                .queryParam("key", nbaApiConfig.getKey())
                .toUriString();
    }
}
