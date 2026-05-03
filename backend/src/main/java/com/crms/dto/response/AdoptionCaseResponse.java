package com.crms.dto.response;

import java.time.LocalDateTime;
import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.enums.AdoptionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCaseResponse {
    
    private String id;
    private String caseRef;
    private AdoptionType adoptionType;
    private Long contractId;
    private String contractRef;
    private Long clientId;
    private String clientName;
    private Long localAuthorityOrWaterAuthorityId;
    private String localAuthorityName;
    private String technicalApprovalRef;
    private BigDecimal designCheckFees;
    private BigDecimal supervisionFees;
    private BigDecimal commutedSumTotal;
    private BigDecimal commutedSumPaid;
    private BigDecimal commutedSumOutstanding;
    private Integer maintenancePeriodMonths;
    private LocalDate commencementDate;
    private LocalDate maintenanceEndDate;
    private AdoptionStatus status;
    private BondResponse bond;
    private List<AdoptionStageResponse> stages;
    private List<CommutedSumMovementResponse> commutedSumMovements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AdoptionCaseResponse fromEntity(AdoptionCase entity) {
        return AdoptionCaseResponse.builder()
                .id(entity.getId().toString())
                .caseRef(entity.getCaseRef())
                .adoptionType(entity.getAdoptionType())
                .contractId(entity.getContract() != null ? entity.getContract().getId() : null)
                .contractRef(entity.getContract() != null ? entity.getContract().getContractRef() : null)
                .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                .clientName(entity.getClient() != null ? entity.getClient().getName() : null)
                .localAuthorityOrWaterAuthorityId(entity.getLocalAuthorityOrWaterAuthority() != null ? entity.getLocalAuthorityOrWaterAuthority().getId() : null)
                .localAuthorityName(entity.getLocalAuthorityOrWaterAuthority() != null ? entity.getLocalAuthorityOrWaterAuthority().getName() : null)
                .technicalApprovalRef(entity.getTechnicalApprovalRef())
                .designCheckFees(entity.getDesignCheckFees())
                .supervisionFees(entity.getSupervisionFees())
                .commutedSumTotal(entity.getCommutedSumTotal())
                .commutedSumPaid(entity.getCommutedSumPaid())
                .commutedSumOutstanding(entity.getCommutedSumOutstanding())
                .maintenancePeriodMonths(entity.getMaintenancePeriodMonths())
                .commencementDate(entity.getCommencementDate())
                .maintenanceEndDate(entity.getMaintenanceEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public static AdoptionCaseResponse fromEntityWithDetails(AdoptionCase entity) {
        AdoptionCaseResponse response = fromEntity(entity);
        
        if (entity.getBond() != null) {
            response.setBond(BondResponse.fromEntity(entity.getBond()));
        }
        
        if (entity.getStages() != null && !entity.getStages().isEmpty()) {
            response.setStages(entity.getStages().stream()
                    .map(AdoptionStageResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        if (entity.getCommutedSumMovements() != null && !entity.getCommutedSumMovements().isEmpty()) {
            response.setCommutedSumMovements(entity.getCommutedSumMovements().stream()
                    .map(CommutedSumMovementResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
