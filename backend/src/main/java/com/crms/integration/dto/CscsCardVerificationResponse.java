package com.crms.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * CSCS Smart Check Card Verification Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CscsCardVerificationResponse {
    
    private String cardNumber;
    private boolean verified;
    private boolean expired;
    private CardStatus status;
    private String schemeName;
    private String cardHolderName;
    private LocalDate cardExpiryDate;
    private String occupation;
    private String competencyRef;
    private List<QualificationDto> qualifications;
    
    public enum CardStatus {
        VALID,
        EXPIRED,
        REVOKED,
        NOT_FOUND,
        PENDING_RENEWAL
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualificationDto {
        private String name;
        private String level;
        private LocalDate expiryDate;
        private boolean valid;
    }
}
