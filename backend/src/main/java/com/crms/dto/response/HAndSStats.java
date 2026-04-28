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
public class HAndSStats {
    
    private Long nearMisses;
    
    private Long minorInjuries;
    
    private Long majorInjuries;
    
    private Long observations;
    
    private BigDecimal afr;
}