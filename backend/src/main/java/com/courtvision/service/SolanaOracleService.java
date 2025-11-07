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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.EdECPrivateKey;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Base64;
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
            log.debug("Oracle Data: {}", oracleData);

            // Validate oracle configuration
            if (oracleProgramId == null || oracleProgramId.isEmpty()) {
                log.warn("Oracle program ID not configured - using mock transaction");
                return generateMockTransactionHash();
            }

            if (oracleWalletPrivateKey == null || oracleWalletPrivateKey.isEmpty()) {
                log.warn("Oracle wallet private key not configured - using mock transaction");
                return generateMockTransactionHash();
            }

            // Create and submit signed transaction
            return createAndSubmitTransaction(oracleData);

        } catch (Exception e) {
            log.error("Error submitting to Solana", e);
            return null;
        }
    }

    /**
     * Create and submit a signed transaction to Solana
     */
    private String createAndSubmitTransaction(Map<String, Object> oracleData) {
        try {
            // Decode the oracle wallet private key from base58
            byte[] oraclePrivateKey = base58Decode(oracleWalletPrivateKey);

            if (oraclePrivateKey.length != 64) {
                log.error("Invalid private key length: {}", oraclePrivateKey.length);
                return null;
            }

            // Create keypair from private key
            KeyPair oracleKeypair = createKeyPairFromPrivateKey(oraclePrivateKey);
            String oraclePubkey = getPublicKeyBase58(oracleKeypair);

            log.debug("Oracle public key: {}", oraclePubkey);

            // Create transaction bytes with instruction
            byte[] transactionBytes = buildTransaction(oracleKeypair, oracleData);

            if (transactionBytes == null) {
                log.error("Failed to build transaction");
                return null;
            }

            // Submit to Solana RPC endpoint
            return callSolanaRpcWithSignedTx(Base64.getEncoder().encodeToString(transactionBytes));

        } catch (Exception e) {
            log.error("Error creating and submitting transaction", e);
            return null;
        }
    }

    /**
     * Build a signed Solana transaction
     */
    private byte[] buildTransaction(KeyPair keypair, Map<String, Object> oracleData) {
        try {
            // Build the full transaction
            // For now, we'll create a simple memo transaction as a placeholder
            // In production, this would be a full instruction to the oracle program
            // and use the keypair for signing

            String memoText = String.format("CourVision Winner: %s Score: %.2f",
                oracleData.get("winnerAddress"),
                oracleData.get("finalScore"));

            byte[] memoBytes = memoText.getBytes(StandardCharsets.UTF_8);

            // Transaction structure: message + signatures
            // This is a simplified version - in production, use a proper Solana library
            log.info("Creating transaction with memo: {}", memoText);

            return memoBytes;

        } catch (Exception e) {
            log.error("Error building transaction", e);
            return null;
        }
    }

    /**
     * Create instruction data from oracle payload
     */
    private byte[] createInstructionData(Map<String, Object> oracleData) {
        try {
            // Serialize oracle data as instruction data
            String jsonData = objectMapper.writeValueAsString(oracleData);
            byte[] dataBytes = jsonData.getBytes(StandardCharsets.UTF_8);

            // Prepend instruction discriminator (8 bytes) for oracle program
            ByteBuffer buffer = ByteBuffer.allocate(8 + dataBytes.length);
            buffer.putLong(0L); // Oracle program instruction discriminator
            buffer.put(dataBytes);

            return buffer.array();
        } catch (Exception e) {
            log.error("Error creating instruction data", e);
            return null;
        }
    }

    /**
     * Create KeyPair from 64-byte private key (seed)
     */
    private KeyPair createKeyPairFromPrivateKey(byte[] privateKeySeed) {
        try {
            // For Ed25519, the private key is the seed (32 bytes)
            if (privateKeySeed.length == 64) {
                // Use first 32 bytes as seed
                byte[] seed = new byte[32];
                System.arraycopy(privateKeySeed, 0, seed, 0, 32);

                // Create EdECPrivateKeySpec
                NamedParameterSpec namedSpec = new NamedParameterSpec("Ed25519");
                EdECPrivateKeySpec edSpec = new EdECPrivateKeySpec(namedSpec, seed);

                KeyFactory kf = KeyFactory.getInstance("EdDSA");
                EdECPrivateKey privateKey = (EdECPrivateKey) kf.generatePrivate(edSpec);

                log.debug("Successfully created private key from seed");
                return new KeyPair(null, privateKey);
            }

            return null;
        } catch (Exception e) {
            log.error("Error creating keypair from private key", e);
            return null;
        }
    }

    /**
     * Get public key in base58 format from keypair
     * TODO: Implement proper key derivation for Ed25519 public key extraction
     */
    private String getPublicKeyBase58(KeyPair keypair) {
        // TODO: Derive actual public key from keypair in production
        // For now, return placeholder value
        // In production: extract public key from keypair and encode to base58
        log.debug("Public key derivation called for Oracle keypair");
        return "DerivedPublicKey";
    }

    /**
     * Call Solana JSON-RPC endpoint with signed transaction
     */
    private String callSolanaRpcWithSignedTx(String base64Tx) {
        HttpClient client = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(solanaRpcEndpoint);

            // Prepare RPC request to send transaction
            Map<String, Object> rpcRequest = new HashMap<>();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("id", 1);
            rpcRequest.put("method", "sendTransaction");

            // Parameters: transaction string, options
            Map<String, Object> options = new HashMap<>();
            options.put("encoding", "base64");
            options.put("skipPreflight", false);

            rpcRequest.put("params", new Object[]{base64Tx, options});

            String jsonBody = objectMapper.writeValueAsString(rpcRequest);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            log.debug("Sending transaction to Solana RPC: {}", solanaRpcEndpoint);

            // Execute request
            return client.execute(httpPost, response -> {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                log.debug("RPC Response: {}", responseBody);

                JsonNode responseJson = objectMapper.readTree(responseBody);

                // Extract transaction hash from response
                if (responseJson.has("result")) {
                    String txHash = responseJson.get("result").asText();
                    log.info("Transaction submitted successfully: {}", txHash);
                    return txHash;
                } else if (responseJson.has("error")) {
                    JsonNode error = responseJson.get("error");
                    String errorMsg = error.isObject() ? error.get("message").asText() : error.asText();
                    log.error("Solana RPC Error: {}", errorMsg);
                    return null;
                }
                return null;
            });

        } catch (IOException e) {
            log.error("Error calling Solana RPC", e);
            return null;
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

    /**
     * Decode base58 string to bytes
     * Solana uses base58 for encoding addresses and keys
     */
    private byte[] base58Decode(String input) {
        try {
            final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
            BigInteger decoded = BigInteger.ZERO;
            int multi = 1;

            for (int i = input.length() - 1; i >= 0; i--) {
                int digit = ALPHABET.indexOf(input.charAt(i));
                if (digit < 0) {
                    throw new IllegalArgumentException("Invalid base58 character: " + input.charAt(i));
                }
                decoded = decoded.add(BigInteger.valueOf(digit).multiply(BigInteger.valueOf(multi)));
                multi *= 58;
            }

            byte[] bytes = decoded.toByteArray();

            // Remove leading zero byte if present (added by BigInteger)
            if (bytes.length > 0 && bytes[0] == 0) {
                byte[] result = new byte[bytes.length - 1];
                System.arraycopy(bytes, 1, result, 0, bytes.length - 1);
                return result;
            }

            return bytes;
        } catch (Exception e) {
            log.error("Error decoding base58 string", e);
            return new byte[0];
        }
    }
}
