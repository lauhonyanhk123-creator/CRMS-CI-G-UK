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
public class RetentionLedgerResponse {
    
    private Long id;
    
    private Long contractId;
    
    private String contractRef;
    
    private BigDecimal totalRetention;
    
    private BigDecimal totalReleased;
    
    private BigDecimal currentRetention;
    
    private Object movements;
}