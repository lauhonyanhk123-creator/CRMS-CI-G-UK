package com.crms.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(nullable = false, length = 10)
    private String action; // POST, PUT, PATCH, DELETE

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "before_state", columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT")
    private String afterState;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 64)
    private String sha256;

    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Computes SHA-256 hash of this audit log entry's content.
     * The hash includes: userId, action, entityType, entityId, beforeState, afterState, timestamp, previousHash
     */
    public void computeHash() {
        StringBuilder data = new StringBuilder();
        data.append(userId != null ? userId.toString() : "");
        data.append("|").append(action != null ? action : "");
        data.append("|").append(entityType != null ? entityType : "");
        data.append("|").append(entityId != null ? entityId : "");
        data.append("|").append(beforeState != null ? beforeState : "");
        data.append("|").append(afterState != null ? afterState : "");
        data.append("|").append(timestamp != null ? timestamp.toString() : "");
        data.append("|").append(previousHash != null ? previousHash : "");
        
        this.sha256 = sha256(data.toString());
    }

    /**
     * Verifies the integrity of this audit log entry by recomputing the hash.
     */
    public boolean verifyHash() {
        if (sha256 == null) {
            return false;
        }
        StringBuilder data = new StringBuilder();
        data.append(userId != null ? userId.toString() : "");
        data.append("|").append(action != null ? action : "");
        data.append("|").append(entityType != null ? entityType : "");
        data.append("|").append(entityId != null ? entityId : "");
        data.append("|").append(beforeState != null ? beforeState : "");
        data.append("|").append(afterState != null ? afterState : "");
        data.append("|").append(timestamp != null ? timestamp.toString() : "");
        data.append("|").append(previousHash != null ? previousHash : "");
        
        return sha256.equals(sha256(data.toString()));
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
