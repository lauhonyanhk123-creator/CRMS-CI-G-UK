package com.crms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayLessNoticeRequest {
    
    @NotNull(message = "Issue date is required")
    private LocalDateTime issuedOn;
    
    @NotNull(message = "Sum considered due is required")
    private BigDecimal sumConsideredDue;

    private BigDecimal amount;

    private String reason;
    
    @Builder.Default
    private String currency = "GBP";
    
    @NotBlank(message = "Basis of calculation is required")
    private String basisOfCalculation;
}