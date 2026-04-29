package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CITB Levy report for construction contracts.
 * CITB Levy = 0.5% of qualifying labour costs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CITBLevyReport {

    private Long contractId;
    private String contractRef;
    private String contractTitle;
    
    // Period covered
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    // Labour costs breakdown
    private BigDecimal operativeWages;           // Live wage data from timesheets/payroll
    private BigDecimal contractLabourValue;     // Fallback: contract labour estimate
    private BigDecimal labourCostsUsed;         // Which value was used (operativeWages or contractLabourValue)
    private boolean usedLiveWageData;           // Flag indicating source of labour costs
    
    // CITB Levy calculation
    private BigDecimal qualifyingLabourCosts;   // Same as labourCostsUsed (qualifying construction work)
    private BigDecimal levyRate;               // 0.5% = 0.005
    private BigDecimal citbLevy;               // Calculated levy amount
    
    // Contract reference data
    private BigDecimal contractValue;
    private BigDecimal valueToDate;
}
