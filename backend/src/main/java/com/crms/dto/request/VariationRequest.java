package com.crms.dto.request;

import com.crms.domain.contract.enums.VariationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VariationRequest {
    
    @NotNull(message = "Variation type is required")
    private VariationType type;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Original value is required")
    private BigDecimal originalValue;
    
    private BigDecimal agreedValue;
    
    private LocalDate notifiedDate;
    
    private String instructionRef;
    
    private String reason;
}