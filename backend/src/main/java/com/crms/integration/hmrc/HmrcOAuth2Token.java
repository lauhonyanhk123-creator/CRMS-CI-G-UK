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
    
    /**
     * Check if the token is expired, with a configurable buffer time.
     * The buffer ensures we refresh before the token actually expires.
     * 
     * @param bufferSeconds additional seconds before expiry to consider token "about to expire"
     * @return true if token is expired or will expire within bufferSeconds
     */
    public boolean isExpired(long bufferSeconds) {
        if (issuedAt == null || expiresIn <= 0) {
            return true;
        }
        return issuedAt.plusSeconds(expiresIn - bufferSeconds).isBefore(Instant.now());
    }
    
    /**
     * Check if the token is expired with default 60-second buffer.
     * @return true if token is expired or will expire within 60 seconds
     */
    public boolean isExpired() {
        return isExpired(60);
    }
    
    /**
     * Check if the token should be refreshed proactively (before it expires).
     * Uses a larger buffer (typically 5 minutes) to refresh before expiry.
     * 
     * @param bufferSeconds seconds before expiry to trigger proactive refresh
     * @return true if token should be refreshed proactively
     */
    public boolean needsProactiveRefresh(long bufferSeconds) {
        return isExpired(bufferSeconds);
    }
    
    /**
     * Get the expiration time as an Instant.
     * @return Instant when the token expires (without buffer), or null if not set
     */
    public Instant getExpirationTime() {
        if (issuedAt == null || expiresIn <= 0) {
            return null;
        }
        return issuedAt.plusSeconds(expiresIn);
    }
    
    /**
     * Get remaining time until token expiry.
     * @return seconds until expiry, or 0 if already expired or not set
     */
    public long getRemainingSeconds() {
        if (issuedAt == null || expiresIn <= 0) {
            return 0;
        }
        Instant expiry = issuedAt.plusSeconds(expiresIn);
        long remaining = expiry.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
