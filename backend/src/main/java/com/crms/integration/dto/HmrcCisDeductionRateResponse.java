package com.crms.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * HMRC CIS Deduction Rate Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmrcCisDeductionRateResponse {
    
    private String supplierUtr;
    private String contractorUtr;
    private BigDecimal deductionRate;
    private String rateType; // "GRS" (Gross), "NET" (Net), or percentage
    private boolean applicable;
    private String calculationBasis;
}
