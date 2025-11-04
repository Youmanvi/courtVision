package com.courtvision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for wallet-based authentication and verification
 * Used to verify wallet ownership through signature
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletAuthRequest {

    private String walletAddress;

    private String message;

    private String signature;
}
