package com.courtvision.util;

import java.util.regex.Pattern;

/**
 * Utility for validating Solana wallet addresses
 * Solana uses base58 encoding for wallet addresses (44 characters)
 * Addresses represent 32-byte public keys
 */
public class SolanaWalletValidator {

    // Solana address is 32 bytes, base58 encoded = approximately 44 characters
    // Base58 excludes: 0 (zero), O (capital o), I (capital i), l (lowercase L)
    private static final Pattern SOLANA_ADDRESS_PATTERN = Pattern.compile("^[1-9A-HJ-NP-Z]{43,44}$");

    private static final int MIN_LENGTH = 43;
    private static final int MAX_LENGTH = 44;

    /**
     * Validate if a string is a valid Solana wallet address
     * @param address The address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSolanaAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        String trimmed = address.trim();

        // Check length
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            return false;
        }

        // Check base58 pattern (excludes: 0, O, I, l)
        return SOLANA_ADDRESS_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validate and normalize wallet address
     * @param address The address to validate
     * @return The validated address (trimmed)
     * @throws IllegalArgumentException if invalid
     */
    public static String validateAndNormalize(String address) {
        if (!isValidSolanaAddress(address)) {
            throw new IllegalArgumentException(
                    "Invalid Solana wallet address format. Expected 43-44 base58 characters."
            );
        }
        return address.trim();
    }
}
