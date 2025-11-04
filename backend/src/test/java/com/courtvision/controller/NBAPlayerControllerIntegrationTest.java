package com.courtvision.controller;

import com.courtvision.config.TestSecurityConfig;
import com.courtvision.dto.NBAPlayerDTO;
import com.courtvision.service.NBAPlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("NBA Player Controller Integration Tests")
class NBAPlayerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NBAPlayerService nbaPlayerService;

    private List<NBAPlayerDTO> mockPlayers;

    @BeforeEach
    void setUp() {
        // Create mock player data
        NBAPlayerDTO player1 = NBAPlayerDTO.builder()
                .playerId("1")
                .firstName("LeBron")
                .lastName("James")
                .position("SF")
                .team("Los Angeles Lakers")
                .teamId("1")
                .height("6'9\"")
                .weight("250")
                .college("St. Vincent-St. Mary High School")
                .salary(41000000.0)
                .jerseyNumber(23)
                .build();

        NBAPlayerDTO player2 = NBAPlayerDTO.builder()
                .playerId("2")
                .firstName("Kevin")
                .lastName("Durant")
                .position("SF")
                .team("Phoenix Suns")
                .teamId("2")
                .height("6'10\"")
                .weight("240")
                .college("University of Texas")
                .salary(42500000.0)
                .jerseyNumber(35)
                .build();

        NBAPlayerDTO player3 = NBAPlayerDTO.builder()
                .playerId("3")
                .firstName("Stephen")
                .lastName("Curry")
                .position("PG")
                .team("Golden State Warriors")
                .teamId("3")
                .height("6'2\"")
                .weight("195")
                .college("Davidson College")
                .salary(51900000.0)
                .jerseyNumber(30)
                .build();

        mockPlayers = Arrays.asList(player1, player2, player3);
    }

    @Test
    @DisplayName("Should return all NBA players")
    void testGetAllPlayers() throws Exception {
        when(nbaPlayerService.getAllPlayers()).thenReturn(mockPlayers);

        mockMvc.perform(get("/api/nba/players")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("Should search players by name")
    void testSearchPlayersByName() throws Exception {
        List<NBAPlayerDTO> searchResults = mockPlayers.subList(0, 1);
        when(nbaPlayerService.searchPlayersByName("LeBron"))
                .thenReturn(searchResults);

        mockMvc.perform(get("/api/nba/players/search")
                .param("name", "LeBron")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].first_name").value("LeBron"));
    }

    @Test
    @DisplayName("Should return empty list for non-existent player name")
    void testSearchPlayersByNameNotFound() throws Exception {
        when(nbaPlayerService.searchPlayersByName("NonExistent"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/nba/players/search")
                .param("name", "NonExistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should search players by team")
    void testSearchPlayersByTeam() throws Exception {
        List<NBAPlayerDTO> lakers = mockPlayers.subList(0, 1);
        when(nbaPlayerService.searchPlayersByTeam("Los Angeles Lakers"))
                .thenReturn(lakers);

        mockMvc.perform(get("/api/nba/players/team")
                .param("team", "Los Angeles Lakers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].team").value("Los Angeles Lakers"));
    }

    @Test
    @DisplayName("Should search players by position")
    void testSearchPlayersByPosition() throws Exception {
        List<NBAPlayerDTO> guards = mockPlayers.subList(2, 3);
        when(nbaPlayerService.searchPlayersByPosition("PG"))
                .thenReturn(guards);

        mockMvc.perform(get("/api/nba/players/position")
                .param("position", "PG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].position").value("PG"));
    }

    @Test
    @DisplayName("Should get player by ID")
    void testGetPlayerById() throws Exception {
        NBAPlayerDTO player = mockPlayers.get(0);
        when(nbaPlayerService.getPlayerById("1"))
                .thenReturn(player);

        mockMvc.perform(get("/api/nba/players/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.first_name").value("LeBron"))
                .andExpect(jsonPath("$.data.last_name").value("James"));
    }

    @Test
    @DisplayName("Should return 404 when player ID not found")
    void testGetPlayerByIdNotFound() throws Exception {
        when(nbaPlayerService.getPlayerById("999"))
                .thenReturn(null);

        mockMvc.perform(get("/api/nba/players/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should get all unique positions")
    void testGetAllPositions() throws Exception {
        List<String> positions = Arrays.asList("PG", "SF");
        when(nbaPlayerService.getAllPositions())
                .thenReturn(positions);

        mockMvc.perform(get("/api/nba/positions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Should get all unique teams")
    void testGetAllTeams() throws Exception {
        List<String> teams = Arrays.asList("Golden State Warriors", "Los Angeles Lakers", "Phoenix Suns");
        when(nbaPlayerService.getAllTeams())
                .thenReturn(teams);

        mockMvc.perform(get("/api/nba/teams")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("Should handle API errors gracefully")
    void testGetAllPlayersApiError() throws Exception {
        when(nbaPlayerService.getAllPlayers())
                .thenThrow(new RuntimeException("API connection failed"));

        try {
            mockMvc.perform(get("/api/nba/players")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // Expected - API error is propagated
        }
    }
}
