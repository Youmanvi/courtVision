package com.courtvision.controller;

import com.courtvision.config.TestSecurityConfig;
import com.courtvision.dto.CreateLeagueRequest;
import com.courtvision.dto.LoginRequest;
import com.courtvision.dto.RegisterRequest;
import com.courtvision.dto.StartDraftRequest;
import com.courtvision.entity.User;
import com.courtvision.repository.DraftPickRepository;
import com.courtvision.repository.DraftRepository;
import com.courtvision.repository.LeagueMemberRepository;
import com.courtvision.repository.LeagueRepository;
import com.courtvision.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DraftController
 * Tests draft creation, draft picking, and draft management functionality
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Draft Controller Integration Tests")
public class DraftControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private LeagueMemberRepository leagueMemberRepository;

    @Autowired
    private DraftRepository draftRepository;

    @Autowired
    private DraftPickRepository draftPickRepository;

    private String jwtToken;
    private User testUser;
    private Long leagueId;

    @BeforeEach
    public void setUp() throws Exception {
        // Clear repositories in correct order
        draftPickRepository.deleteAll();
        draftRepository.deleteAll();
        leagueMemberRepository.deleteAll();
        leagueRepository.deleteAll();
        userRepository.deleteAll();

        // Create and authenticate test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("draftuser");
        registerRequest.setEmail("draft@example.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("draftuser");
        loginRequest.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
        jwtToken = jsonNode.get("data").get("accessToken").asText();

        // Get user from database
        testUser = userRepository.findByUsername("draftuser").orElse(null);
        assertNotNull(testUser);

        // Create a league for draft testing
        CreateLeagueRequest leagueRequest = new CreateLeagueRequest();
        leagueRequest.setName("Draft Test League");
        leagueRequest.setDescription("League for testing drafts");
        leagueRequest.setMaxPlayers(4);

        MvcResult leagueResult = mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leagueRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String leagueBody = leagueResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode leagueJson = objectMapper.readTree(leagueBody);
        leagueId = leagueJson.get("data").get("id").asLong();
    }

    @Test
    @DisplayName("Start draft successfully with valid request")
    public void testStartDraftSuccess() throws Exception {
        // Arrange
        StartDraftRequest request = new StartDraftRequest();
        request.setRoundsPerTeam(3);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Draft started successfully"))
                .andExpect(jsonPath("$.data.leagueId").value(leagueId.intValue()))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.currentRound").value(1))
                .andExpect(jsonPath("$.data.roundsPerTeam").value(3))
                .andExpect(jsonPath("$.data.totalPicks").value(3)) // 1 team (creator) * 3 rounds
                .andReturn();

        // Verify draft was created in database
        assertEquals(1, draftRepository.count());
    }

    @Test
    @DisplayName("Get draft for league")
    public void testGetDraftSuccess() throws Exception {
        // Arrange - Create a draft first
        StartDraftRequest startRequest = new StartDraftRequest();
        startRequest.setRoundsPerTeam(2);

        mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Get the draft
        mockMvc.perform(get("/api/drafts/leagues/{leagueId}", leagueId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leagueId").value(leagueId.intValue()))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("Get draft not found returns 404")
    public void testGetDraftNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/drafts/leagues/{leagueId}", 999999L)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Start draft fails when not league creator")
    public void testStartDraftUnauthorized() throws Exception {
        // Arrange - Create another user
        RegisterRequest otherUserRequest = new RegisterRequest();
        otherUserRequest.setUsername("otheruser");
        otherUserRequest.setEmail("other@example.com");
        otherUserRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherUserRequest)))
                .andExpect(status().isCreated());

        // Login as other user
        LoginRequest otherLoginRequest = new LoginRequest();
        otherLoginRequest.setUsername("otheruser");
        otherLoginRequest.setPassword("Password123!");

        MvcResult otherResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String otherBody = otherResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode otherJson = objectMapper.readTree(otherBody);
        String otherToken = otherJson.get("data").get("accessToken").asText();

        // Try to start draft as non-creator
        StartDraftRequest request = new StartDraftRequest();
        request.setRoundsPerTeam(3);

        mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Start draft with default rounds")
    public void testStartDraftWithDefaults() throws Exception {
        // Arrange - No roundsPerTeam specified
        StartDraftRequest request = new StartDraftRequest();

        // Act & Assert
        mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roundsPerTeam").value(3)); // Default is 3
    }

    @Test
    @DisplayName("Get draft picks for league")
    public void testGetDraftPicksEmpty() throws Exception {
        // Arrange - Create a draft
        StartDraftRequest startRequest = new StartDraftRequest();
        startRequest.setRoundsPerTeam(1);

        mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated());

        // Get draft ID
        MvcResult getDraftResult = mockMvc.perform(get("/api/drafts/leagues/{leagueId}", leagueId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String getDraftBody = getDraftResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode getDraftJson = objectMapper.readTree(getDraftBody);
        Long draftId = getDraftJson.get("data").get("id").asLong();

        // Act & Assert - Get draft picks
        mockMvc.perform(get("/api/drafts/{draftId}/picks", draftId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0))); // No picks yet
    }

    @Test
    @DisplayName("Cannot start draft twice for same league")
    public void testStartDraftDuplicate() throws Exception {
        // Arrange - Start first draft
        StartDraftRequest request = new StartDraftRequest();
        request.setRoundsPerTeam(3);

        mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to start another draft
        mockMvc.perform(post("/api/drafts/leagues/{leagueId}/start", leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
