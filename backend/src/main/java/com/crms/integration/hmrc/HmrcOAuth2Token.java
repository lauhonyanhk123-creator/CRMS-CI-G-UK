package com.crms.integration.hmrc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * OAuth2 token holder for HMRC API authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmrcOAuth2Token {
    
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Instant issuedAt;
    
    public boolean isExpired() {
        return issuedAt.plusSeconds(expiresIn - 60).isBefore(Instant.now()); // 60s buffer
    }
}
