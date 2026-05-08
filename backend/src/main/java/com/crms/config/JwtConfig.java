package com.crms.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret;
    private long expiration;
    private long refreshExpiration;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            log.error("JWT secret is not configured. Set the JWT_SECRET environment variable.");
            throw new IllegalStateException("JWT secret must not be blank");
        }
        if (secret.length() < 32) {
            log.error("JWT secret is too short ({} chars). Must be at least 32 characters.", secret.length());
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
    }
}
