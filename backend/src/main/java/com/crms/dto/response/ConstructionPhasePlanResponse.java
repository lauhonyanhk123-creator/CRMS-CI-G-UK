package com.crms.dto.response;

import com.crms.domain.healthsafety.entity.ConstructionPhasePlan;
import com.crms.domain.healthsafety.enums.CppStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionPhasePlanResponse {

    private Long id;
    private Long contractId;
    private String contractRef;
    private String planRef;
    private String version;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private CppStatus status;
    private String approvedBy;
    private LocalDate approvedDate;
    private String documentRef;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConstructionPhasePlanResponse fromEntity(ConstructionPhasePlan entity) {
        return ConstructionPhasePlanResponse.builder()
                .id(entity.getId())
                .contractId(entity.getContract() != null ? entity.getContract().getId() : null)
                .contractRef(entity.getContract() != null ? entity.getContract().getContractRef() : null)
                .planRef(entity.getPlanRef())
                .version(entity.getVersion())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .approvedBy(entity.getApprovedBy())
                .approvedDate(entity.getApprovedDate())
                .documentRef(entity.getDocumentRef())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
