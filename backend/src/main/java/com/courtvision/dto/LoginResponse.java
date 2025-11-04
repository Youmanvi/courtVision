package com.courtvision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType = "Bearer";

    private String username;

    @JsonProperty("wallet_address")
    private String walletAddress;

    @JsonProperty("wallet_verified")
    private Boolean walletVerified;

    @JsonProperty("user_id")
    private Long userId;
}
