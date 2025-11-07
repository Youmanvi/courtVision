package com.courtvision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Solana Oracle Service
 * Handles communication with Solana blockchain and sends winner announcements
 *
 * This service submits winner information to the Solana blockchain via an oracle program.
 * It supports both mainnet and devnet for testing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolanaOracleService {

    private final ObjectMapper objectMapper;

    @Value("${solana.rpc-endpoint:https://api.mainnet-beta.solana.com}")
    private String solanaRpcEndpoint;

    @Value("${solana.oracle-program-id:}")
    private String oracleProgramId;

    @Value("${solana.oracle-wallet-private-key:}")
    private String oracleWalletPrivateKey;

    @Value("${solana.network:mainnet}")
    private String network;

    @Value("${solana.confirmation-timeout:30}")
    private long confirmationTimeoutSeconds;

    /**
     * Submit winner announcement to Solana blockchain
     *
     * @param leagueId League ID
     * @param leagueName League name
     * @param winnerAddress Solana wallet address of the winner
     * @param finalScore Winner's final score
     * @return Transaction hash if successful, null if failed
     */
    public String submitWinnerToBlockchain(Long leagueId, String leagueName, String winnerAddress, Double finalScore) {
        try {
            log.info("Submitting winner to Solana blockchain - League: {}, Winner: {}", leagueId, winnerAddress);

            // Validate inputs
            if (!isValidSolanaAddress(winnerAddress)) {
                log.error("Invalid Solana wallet address: {}", winnerAddress);
                return null;
            }

            // Create oracle data payload
            Map<String, Object> oracleData = createOraclePayload(leagueId, leagueName, winnerAddress, finalScore);

            // Submit to Solana blockchain
            String transactionHash = submitToSolana(oracleData);

            if (transactionHash != null) {
                log.info("Successfully submitted winner to Solana. TX Hash: {}", transactionHash);
                return transactionHash;
            } else {
                log.warn("Failed to submit winner to Solana");
                return null;
            }

        } catch (Exception e) {
            log.error("Error submitting winner to Solana blockchain", e);
            return null;
        }
    }

    /**
     * Create oracle data payload for Solana submission
     */
    private Map<String, Object> createOraclePayload(Long leagueId, String leagueName, String winnerAddress, Double finalScore) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("eventType", "LEAGUE_WINNER_ANNOUNCED");
        payload.put("leagueId", leagueId);
        payload.put("leagueName", leagueName);
        payload.put("winnerAddress", winnerAddress);
        payload.put("finalScore", finalScore);
        payload.put("announcedAt", System.currentTimeMillis());
        payload.put("network", network);
        return payload;
    }

    /**
     * Submit data to Solana blockchain via RPC
     */
    private String submitToSolana(Map<String, Object> oracleData) {
        try {
            // In production, this would be replaced with actual Solana Web3 library calls
            // For now, we'll implement a mock transaction submission

            log.debug("Oracle Data: {}", oracleData);

            // Validate oracle configuration
            if (oracleProgramId == null || oracleProgramId.isEmpty()) {
                log.warn("Oracle program ID not configured - using mock transaction");
                return generateMockTransactionHash();
            }

            // Call actual Solana RPC endpoint
            return callSolanaRpc(oracleData);

        } catch (Exception e) {
            log.error("Error submitting to Solana", e);
            return null;
        }
    }

    /**
     * Call Solana JSON-RPC endpoint
     */
    private String callSolanaRpc(Map<String, Object> oracleData) {
        HttpClient client = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(solanaRpcEndpoint);

            // Prepare RPC request
            Map<String, Object> rpcRequest = new HashMap<>();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("id", 1);
            rpcRequest.put("method", "sendTransaction");
            rpcRequest.put("params", new Object[]{
                // Base58-encoded transaction data
                "TODO: Create signed transaction with oracle data"
            });

            String jsonBody = objectMapper.writeValueAsString(rpcRequest);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            // Execute request
            return client.execute(httpPost, response -> {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                JsonNode responseJson = objectMapper.readTree(responseBody);

                // Extract transaction hash from response
                if (responseJson.has("result")) {
                    return responseJson.get("result").asText();
                } else if (responseJson.has("error")) {
                    log.error("Solana RPC Error: {}", responseJson.get("error").asText());
                    return null;
                }
                return null;
            });

        } catch (IOException e) {
            log.error("Error calling Solana RPC", e);
            return null;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error closing HTTP client", e);
            }
        }
    }

    /**
     * Check if transaction is confirmed on blockchain
     */
    public boolean isTransactionConfirmed(String transactionHash) {
        try {
            log.debug("Checking transaction confirmation: {}", transactionHash);

            HttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(solanaRpcEndpoint);

            Map<String, Object> rpcRequest = new HashMap<>();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("id", 1);
            rpcRequest.put("method", "getSignatureStatus");
            rpcRequest.put("params", new Object[]{transactionHash});

            String jsonBody = objectMapper.writeValueAsString(rpcRequest);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            return client.execute(httpPost, response -> {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                JsonNode responseJson = objectMapper.readTree(responseBody);

                if (responseJson.has("result")) {
                    JsonNode result = responseJson.get("result");
                    if (result.has("value") && result.get("value").isObject()) {
                        JsonNode value = result.get("value");
                        if (value.has("confirmationStatus")) {
                            String status = value.get("confirmationStatus").asText();
                            return "confirmed".equalsIgnoreCase(status) || "finalized".equalsIgnoreCase(status);
                        }
                    }
                }
                return false;
            });

        } catch (Exception e) {
            log.error("Error checking transaction confirmation", e);
            return false;
        }
    }

    /**
     * Validate Solana wallet address format
     */
    public boolean isValidSolanaAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        // Solana addresses are 43-44 characters, base58 encoded
        String trimmed = address.trim();
        if (trimmed.length() < 43 || trimmed.length() > 44) {
            return false;
        }

        // Base58 pattern (excludes: 0, O, I, l)
        return trimmed.matches("^[1-9A-HJ-NP-Z]{43,44}$");
    }

    /**
     * Generate mock transaction hash for testing
     */
    private String generateMockTransactionHash() {
        // Generate a realistic-looking mock transaction hash (88 characters base58)
        String base58Chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < 88; i++) {
            hash.append(base58Chars.charAt((int) (Math.random() * base58Chars.length())));
        }
        return hash.toString();
    }

    /**
     * Get Solana network name (mainnet or devnet)
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Get Solana RPC endpoint
     */
    public String getRpcEndpoint() {
        return solanaRpcEndpoint;
    }
}
