package com.crms.dto.request;

import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.enums.AdoptionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCaseRequest {
    
    @NotNull(message = "Case reference is required")
    private String caseRef;
    
    @NotNull(message = "Adoption type is required")
    private AdoptionType adoptionType;
    
    @NotNull(message = "Contract ID is required")
    private Long contractId;
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    @NotNull(message = "Local authority/Water authority ID is required")
    private Long localAuthorityOrWaterAuthorityId;
    
    private String technicalApprovalRef;
    
    private BigDecimal designCheckFees;
    
    private BigDecimal supervisionFees;
    
    private BigDecimal commutedSumTotal;
    
    private Integer maintenancePeriodMonths;
    
    private LocalDate commencementDate;
    
    private LocalDate maintenanceEndDate;
    
    private AdoptionStatus status;
}
