package com.crms.dto.request;

import com.crms.domain.adoption.enums.CommutedSumType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommutedSumMovementRequest {
    
    @NotNull(message = "Movement date is required")
    private LocalDate movementDate;
    
    @NotNull(message = "Type is required")
    private CommutedSumType type;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    private String reason;
    
    private String documentRef;
}
