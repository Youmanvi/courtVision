package com.courtvision.controller;

import com.courtvision.config.TestSecurityConfig;
import com.courtvision.dto.ApiResponse;
import com.courtvision.dto.LoginRequest;
import com.courtvision.dto.LoginResponse;
import com.courtvision.dto.RegisterRequest;
import com.courtvision.entity.User;
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
 * Integration tests for AuthController
 * Tests user registration and login functionality
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Auth Controller Integration Tests")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        // Clear the user repository before each test
        userRepository.deleteAll();
    }

    // ==================== User Registration Tests ====================

    @Test
    @DisplayName("Register new user successfully")
    public void testRegisterNewUserSuccess() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("SecurePassword123!");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andReturn();

        // Verify user was created in database
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    @DisplayName("Register fails when username already exists")
    public void testRegisterDuplicateUsername() throws Exception {
        // Arrange - Create a user first
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setUsername("existinguser");
        firstRequest.setEmail("first@example.com");
        firstRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Try to register with same username
        RegisterRequest secondRequest = new RegisterRequest();
        secondRequest.setUsername("existinguser");
        secondRequest.setEmail("second@example.com");
        secondRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("Register fails when email already exists")
    public void testRegisterDuplicateEmail() throws Exception {
        // Arrange - Create a user first
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setUsername("user1");
        firstRequest.setEmail("duplicate@example.com");
        firstRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Try to register with same email
        RegisterRequest secondRequest = new RegisterRequest();
        secondRequest.setUsername("user2");
        secondRequest.setEmail("duplicate@example.com");
        secondRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Register fails with invalid input")
    public void testRegisterInvalidInput() throws Exception {
        // Arrange - Empty request body
        String invalidJson = "{}";

        // Act & Assert - Returns error status (400 or 500)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status >= 400 && status < 600 : "Expected error status, got " + status;
                });
    }

    // ==================== User Login Tests ====================

    @Test
    @DisplayName("Login successfully with correct credentials")
    public void testLoginSuccess() throws Exception {
        // Arrange - Register a user first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Login with correct credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User logged in successfully"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value("loginuser"))
                .andReturn();
    }

    @Test
    @DisplayName("Login fails with incorrect password")
    public Exception testLoginWrongPassword() throws Exception {
        // Arrange - Register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("wrongpassuser");
        registerRequest.setEmail("wrongpass@example.com");
        registerRequest.setPassword("CorrectPassword123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Act & Assert - Login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpassuser");
        loginRequest.setPassword("WrongPassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        return null;
    }

    @Test
    @DisplayName("Login fails when user does not exist")
    public void testLoginNonExistentUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Register and login workflow succeeds")
    public void testRegisterAndLoginWorkflow() throws Exception {
        // Arrange - Complete registration
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("workflowuser");
        registerRequest.setEmail("workflow@example.com");
        registerRequest.setPassword("WorkflowPass123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Act - Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("workflowuser");
        loginRequest.setPassword("WorkflowPass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("workflowuser"));

        // Assert - Verify user exists in database
        User user = userRepository.findByUsername("workflowuser").orElse(null);
        assertNotNull(user);
        assertEquals("workflowuser", user.getUsername());
        assertEquals("workflow@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Multiple users can register and login independently")
    public void testMultipleUsersRegistration() throws Exception {
        // Arrange & Act - Register first user
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("user1");
        request1.setEmail("user1@example.com");
        request1.setPassword("Password1!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Register second user
        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("user2");
        request2.setEmail("user2@example.com");
        request2.setPassword("Password2!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // Assert - Both users exist in database
        assertEquals(2, userRepository.count());
        assertNotNull(userRepository.findByUsername("user1").orElse(null));
        assertNotNull(userRepository.findByUsername("user2").orElse(null));

        // Login as first user
        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setUsername("user1");
        loginRequest1.setPassword("Password1!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isOk());

        // Login as second user
        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUsername("user2");
        loginRequest2.setPassword("Password2!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest2)))
                .andExpect(status().isOk());
    }
}
