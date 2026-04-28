package com.crms.dto.response;

import com.crms.domain.adoption.entity.SnaggingItem;
import com.crms.domain.adoption.enums.SnaggingItemPriority;
import com.crms.domain.adoption.enums.SnaggingItemStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnaggingItemResponse {
    
    private String id;
    private Long adoptionCaseId;
    private String caseRef;
    private String description;
    private String location;
    private SnaggingItemPriority priority;
    private LocalDate identifiedDate;
    private LocalDate targetCompletionDate;
    private LocalDate actualCompletionDate;
    private SnaggingItemStatus status;
    private String notes;
    private String assignedTo;
    private LocalDate verifiedDate;
    private String verifiedBy;
    private boolean overdue;
    
    public static SnaggingItemResponse fromEntity(SnaggingItem entity) {
        return SnaggingItemResponse.builder()
                .id(entity.getId().toString())
                .adoptionCaseId(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getId() : null)
                .caseRef(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getCaseRef() : null)
                .description(entity.getDescription())
                .location(entity.getLocation())
                .priority(entity.getPriority())
                .identifiedDate(entity.getIdentifiedDate())
                .targetCompletionDate(entity.getTargetCompletionDate())
                .actualCompletionDate(entity.getActualCompletionDate())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .assignedTo(entity.getAssignedTo())
                .verifiedDate(entity.getVerifiedDate())
                .verifiedBy(entity.getVerifiedBy())
                .overdue(entity.isOverdue())
                .build();
    }
}
