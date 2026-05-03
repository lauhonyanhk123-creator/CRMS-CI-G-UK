package com.crms.dto.request;

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
public class ApplicationForPaymentRequest {
    
    @NotNull(message = "Application period start is required")
    private LocalDate applicationPeriodStart;
    
    @NotNull(message = "Application period end is required")
    private LocalDate applicationPeriodEnd;

    private LocalDate dueDate;
    
    @NotNull(message = "Value of works is required")
    private BigDecimal valueOfWorks;
    
    private BigDecimal retention;
    
    private BigDecimal grossValue;
    
    private String notes;
}