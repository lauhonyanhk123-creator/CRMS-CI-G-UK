package com.crms.dto.request;

import com.crms.domain.adoption.enums.BondStatus;
import com.crms.domain.adoption.enums.BondType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BondRequest {
    
    @NotNull(message = "Bond number is required")
    private String bondNumber;
    
    @NotNull(message = "Bond type is required")
    private BondType bondType;
    
    @NotNull(message = "Issuing surety ID is required")
    private Long issuingSuretyId;
    
    @NotNull(message = "Bond value is required")
    private BigDecimal bondValue;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;
    
    private String releaseConditions;
    
    private LocalDate releaseDate;
    
    private BondStatus status;
}
