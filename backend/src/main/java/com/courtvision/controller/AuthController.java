package com.courtvision.controller;

import com.courtvision.dto.*;
import com.courtvision.entity.User;
import com.courtvision.repository.UserRepository;
import com.courtvision.security.JwtTokenProvider;
import com.courtvision.util.SolanaWalletValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user with Solana wallet
     * Wallet address is validated and stored securely
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Validate and normalize Solana wallet address
            String validatedWallet;
            try {
                validatedWallet = SolanaWalletValidator.validateAndNormalize(
                        registerRequest.getSolanaWallet()
                );
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("Invalid Solana wallet address: " + e.getMessage())
                                .build());
            }

            // Check if username already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("Username already exists")
                                .build());
            }

            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("Email already exists")
                                .build());
            }

            // Check if wallet address already registered
            if (userRepository.existsBySolanaWallet(validatedWallet)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("This Solana wallet is already registered")
                                .build());
            }

            // Create new user with wallet address
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .solanaWallet(validatedWallet)
                    .walletVerified(false)
                    .build();

            userRepository.save(user);

            log.info("User registered successfully: {} with wallet: {}",
                    registerRequest.getUsername(), validatedWallet);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("User registered successfully. Please verify your wallet.")
                            .data(user.getId())
                            .build());

        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Login user and return JWT token with wallet information
     * JWT token includes wallet address for secure, stateless authentication
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Generate JWT token (includes wallet address in claims)
            String token = jwtTokenProvider.generateToken(authentication);

            // Build and return response with wallet information
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .walletAddress(user.getSolanaWallet())
                    .walletVerified(user.getWalletVerified())
                    .userId(user.getId())
                    .build();

            log.info("User logged in successfully: {} | Wallet: {}",
                    loginRequest.getUsername(), user.getSolanaWallet());

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("User logged in successfully")
                            .data(loginResponse)
                            .build());

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Invalid username or password")
                            .build());
        } catch (Exception e) {
            log.error("Error logging in user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Login failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get current user information (requires authentication)
     * Returns user details including wallet information from JWT
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.builder()
                                .success(false)
                                .message("Not authenticated")
                                .build());
            }

            User user = (User) authentication.getPrincipal();

            return ResponseEntity.ok()
                    .body(ApiResponse.builder()
                            .success(true)
                            .message("Current user info")
                            .data(user)
                            .build());
        } catch (Exception e) {
            log.error("Error fetching current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Failed to fetch user info: " + e.getMessage())
                            .build());
        }
    }
}
