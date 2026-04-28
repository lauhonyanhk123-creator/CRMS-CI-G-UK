package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRItem {
    
    private Long contractId;
    
    private String contractName;
    
    private BigDecimal valueToDate;
    
    private BigDecimal costToDate;
    
    private BigDecimal grossMargin;
    
    private BigDecimal marginPercent;
}