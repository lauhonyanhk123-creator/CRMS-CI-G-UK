package com.crms.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * HMRC CIS Verification Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmrcCisVerificationResponse {
    
    private String verificationRef;
    private CisVerificationResult result;
    private BigDecimal deductionRate;
    private LocalDate verifiedAt;
    private LocalDate expiresAt;
    private String companyName;
    private String utr;
    
    /**
     * Indicates this response was returned from cached data due to service being offline.
     */
    @Builder.Default
    private boolean offlineData = false;
    
    public enum CisVerificationResult {
        VERIFIED,
        VERIFIED_WITH_WARNINGS,
        NOT_VERIFIED,
        UTR_NOT_FOUND,
        COMPANY_MISMATCH
    }
}
