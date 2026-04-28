package com.crms.integration.hmrc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for submitting CIS monthly returns to HMRC.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CisReturnDto {
    
    private String taxMonth; // Format: "2026-04" (YYYY-MM)
    private String contractorUtr;
    private String submissionRef;
    private List<CisReturnLineDto> lines;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CisReturnLineDto {
        private String subcontractorUtr;
        private String subcontractorName;
        private String subcontractorUTR;
        private LocalDate paymentDate;
        private BigDecimal grossAmount;
        private BigDecimal deductionAmount;
        private BigDecimal rate;
        private String transactionRef;
    }
}
