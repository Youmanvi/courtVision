package com.courtvision.controller;

import com.courtvision.config.TestSecurityConfig;
import com.courtvision.dto.CreateLeagueRequest;
import com.courtvision.dto.LoginRequest;
import com.courtvision.dto.RegisterRequest;
import com.courtvision.entity.League;
import com.courtvision.entity.User;
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
 * Integration tests for LeagueController
 * Tests league creation, retrieval, and management functionality
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("League Controller Integration Tests")
public class LeagueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private com.courtvision.repository.LeagueMemberRepository leagueMemberRepository;

    @Autowired
    private com.courtvision.repository.LeagueInvitationRepository leagueInvitationRepository;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    public void setUp() throws Exception {
        // Clear repositories (delete in correct order to respect foreign keys)
        leagueInvitationRepository.deleteAll();
        leagueMemberRepository.deleteAll();
        leagueRepository.deleteAll();
        userRepository.deleteAll();

        // Create and authenticate test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("leagueuser");
        registerRequest.setEmail("league@example.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("leagueuser");
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
        testUser = userRepository.findByUsername("leagueuser").orElse(null);
        assertNotNull(testUser);
    }

    // ==================== League Creation Tests ====================

    @Test
    @DisplayName("Create league successfully with valid input")
    public void testCreateLeagueSuccess() throws Exception {
        // Arrange
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Fantasy Basketball League");
        request.setDescription("A competitive fantasy basketball league");
        request.setMaxPlayers(8);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("League created successfully"))
                .andExpect(jsonPath("$.data.name").value("Fantasy Basketball League"))
                .andExpect(jsonPath("$.data.description").value("A competitive fantasy basketball league"))
                .andExpect(jsonPath("$.data.maxPlayers").value(8))
                .andExpect(jsonPath("$.data.currentMemberCount").value(1))
                .andReturn();

        // Verify league was created in database
        assertEquals(1, leagueRepository.count());
        League league = leagueRepository.findByCreatorId(testUser.getId()).get(0);
        assertEquals("Fantasy Basketball League", league.getName());
        assertEquals(8, league.getMaxPlayers());
    }

    @Test
    @DisplayName("Create league fails when name is too short")
    public void testCreateLeagueNameTooShort() throws Exception {
        // Arrange - Name with less than 3 characters
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("AB");
        request.setDescription("Short name league");
        request.setMaxPlayers(4);

        // Act & Assert
        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Create league fails when name is too long")
    public void testCreateLeagueNameTooLong() throws Exception {
        // Arrange - Name with more than 100 characters
        String longName = "A".repeat(101);
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName(longName);
        request.setDescription("Long name league");
        request.setMaxPlayers(4);

        // Act & Assert
        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Create league fails when maxPlayers is less than 2")
    public void testCreateLeagueMaxPlayersTooLow() throws Exception {
        // Arrange
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Small League");
        request.setDescription("Too small league");
        request.setMaxPlayers(1);

        // Act & Assert
        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Create league fails when maxPlayers is more than 8")
    public void testCreateLeagueMaxPlayersTooHigh() throws Exception {
        // Arrange
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Too Large League");
        request.setDescription("Too large league");
        request.setMaxPlayers(9);

        // Act & Assert
        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Create league fails when not authenticated")
    public void testCreateLeagueUnauthorized() throws Exception {
        // Arrange
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Unauthorized League");
        request.setDescription("Test league");
        request.setMaxPlayers(4);

        // Act & Assert - No authorization header returns 401 or 403
        mockMvc.perform(post("/api/leagues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert (status == 401 || status == 403) : "Expected 401 or 403, got " + status;
                });
    }

    // ==================== League Retrieval Tests ====================

    @Test
    @DisplayName("Get all leagues for authenticated user")
    public void testGetUserLeaguesSuccess() throws Exception {
        // Arrange - Create a league first
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("User League 1");
        request.setDescription("First league");
        request.setMaxPlayers(4);

        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Act & Assert - Get all leagues
        mockMvc.perform(get("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("User League 1"));
    }

    @Test
    @DisplayName("Get league by ID successfully")
    public void testGetLeagueByIdSuccess() throws Exception {
        // Arrange - Create a league
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("League to Retrieve");
        request.setDescription("Test retrieval");
        request.setMaxPlayers(6);

        MvcResult createResult = mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long leagueId = jsonNode.get("data").get("id").asLong();

        // Act & Assert - Get league by ID
        mockMvc.perform(get("/api/leagues/" + leagueId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("League to Retrieve"))
                .andExpect(jsonPath("$.data.maxPlayers").value(6));
    }

    @Test
    @DisplayName("Get non-existent league returns 404")
    public void testGetLeagueNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/leagues/999999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== League Update Tests ====================

    @Test
    @DisplayName("Update league successfully as creator")
    public void testUpdateLeagueSuccess() throws Exception {
        // Arrange - Create a league
        CreateLeagueRequest createRequest = new CreateLeagueRequest();
        createRequest.setName("Original Name");
        createRequest.setDescription("Original description");
        createRequest.setMaxPlayers(4);

        MvcResult createResult = mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long leagueId = jsonNode.get("data").get("id").asLong();

        // Act - Update league
        CreateLeagueRequest updateRequest = new CreateLeagueRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated description");
        updateRequest.setMaxPlayers(6);

        mockMvc.perform(put("/api/leagues/" + leagueId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.maxPlayers").value(6));
    }

    @Test
    @DisplayName("Create multiple leagues successfully")
    public void testCreateMultipleLeagues() throws Exception {
        // Arrange & Act - Create first league
        CreateLeagueRequest request1 = new CreateLeagueRequest();
        request1.setName("League One");
        request1.setDescription("First league");
        request1.setMaxPlayers(4);

        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Create second league
        CreateLeagueRequest request2 = new CreateLeagueRequest();
        request2.setName("League Two");
        request2.setDescription("Second league");
        request2.setMaxPlayers(8);

        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Assert - Both leagues exist
        mockMvc.perform(get("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].name", containsInAnyOrder("League One", "League Two")));
    }

    @Test
    @DisplayName("League creator is automatically added as owner")
    public void testLeagueCreatorIsOwner() throws Exception {
        // Arrange & Act - Create a league
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Creator Test League");
        request.setDescription("Testing creator role");
        request.setMaxPlayers(4);

        MvcResult result = mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long leagueId = jsonNode.get("data").get("id").asLong();

        // Assert - Get league members and verify creator is owner
        mockMvc.perform(get("/api/leagues/" + leagueId + "/members")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"));
    }

    @Test
    @DisplayName("Create league with minimum player count")
    public void testCreateLeagueMinPlayers() throws Exception {
        // Arrange
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Minimal League");
        request.setDescription("Minimum players");
        request.setMaxPlayers(2);

        // Act & Assert
        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maxPlayers").value(2));
    }

    @Test
    @DisplayName("Create league with maximum player count")
    public void testCreateLeagueMaxPlayers() throws Exception {
        // Arrange
        CreateLeagueRequest request = new CreateLeagueRequest();
        request.setName("Maximal League");
        request.setDescription("Maximum players");
        request.setMaxPlayers(8);

        // Act & Assert
        mockMvc.perform(post("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maxPlayers").value(8));
    }

    @Test
    @DisplayName("Get user leagues returns empty list when no leagues exist")
    public void testGetUserLeaguesEmpty() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/leagues")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
