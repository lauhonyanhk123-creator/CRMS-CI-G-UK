package com.crms.dto.response;

import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.enums.StageStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionStageResponse {
    
    private String id;
    private Long adoptionCaseId;
    private String caseRef;
    private String stageName;
    private Integer stageOrder;
    private LocalDate plannedDate;
    private LocalDate actualDate;
    private StageStatus status;
    private String notes;
    private boolean overdue;
    
    public static AdoptionStageResponse fromEntity(AdoptionStage entity) {
        return AdoptionStageResponse.builder()
                .id(entity.getId().toString())
                .adoptionCaseId(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getId() : null)
                .caseRef(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getCaseRef() : null)
                .stageName(entity.getStageName())
                .stageOrder(entity.getStageOrder())
                .plannedDate(entity.getPlannedDate())
                .actualDate(entity.getActualDate())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .overdue(entity.isOverdue())
                .build();
    }
}
