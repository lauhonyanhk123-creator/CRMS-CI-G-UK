package com.crms.integration.hmrc;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "hmrc_oauth_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HmrcOAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contractor_utr", nullable = false, unique = true, length = 10)
    private String contractorUtr;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_type", nullable = false, length = 32)
    @Builder.Default
    private String tokenType = "Bearer";

    @Column(name = "expires_in", nullable = false)
    private Long expiresIn;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isAccessTokenExpired(int bufferSeconds) {
        if (issuedAt == null || expiresIn == null) return true;
        return Instant.now().isAfter(issuedAt.plusSeconds(expiresIn - bufferSeconds));
    }
}
