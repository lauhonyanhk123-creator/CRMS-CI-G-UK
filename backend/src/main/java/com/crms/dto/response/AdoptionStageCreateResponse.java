package com.crms.dto.response;

import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.enums.StageStatus;
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
public class AdoptionStageCreateResponse {

    private Long id;
    private Long caseId;
    private String caseRef;
    private String stageName;
    private String description;
    private Integer stageOrder;
    private LocalDate targetDate;
    private LocalDate completedDate;
    private StageStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdoptionStageCreateResponse fromEntity(AdoptionStage entity) {
        return AdoptionStageCreateResponse.builder()
                .id(entity.getId())
                .caseId(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getId() : null)
                .caseRef(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getCaseRef() : null)
                .stageName(entity.getStageName())
                .description(entity.getDescription())
                .stageOrder(entity.getStageOrder())
                .targetDate(entity.getPlannedDate())
                .completedDate(entity.getActualDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
