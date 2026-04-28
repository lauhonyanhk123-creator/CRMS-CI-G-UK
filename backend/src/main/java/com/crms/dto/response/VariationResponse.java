package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationResponse {
    
    private Long id;
    private String variationRef;
    private Long contractId;
    private String contractRef;
    private String type;
    private String description;
    private BigDecimal originalValue;
    private BigDecimal agreedValue;
    private LocalDate notifiedDate;
    private String status;
    private String instructionRef;
    private String reason;
}