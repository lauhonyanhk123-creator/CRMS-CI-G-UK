package com.crms.dto.response;

import com.crms.domain.adoption.entity.CommutedSumMovement;
import com.crms.domain.adoption.enums.CommutedSumType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommutedSumMovementResponse {
    
    private String id;
    private Long adoptionCaseId;
    private String caseRef;
    private LocalDate movementDate;
    private CommutedSumType type;
    private BigDecimal amount;
    private String reason;
    private String documentRef;
    
    public static CommutedSumMovementResponse fromEntity(CommutedSumMovement entity) {
        return CommutedSumMovementResponse.builder()
                .id(entity.getId().toString())
                .adoptionCaseId(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getId() : null)
                .caseRef(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getCaseRef() : null)
                .movementDate(entity.getMovementDate())
                .type(entity.getType())
                .amount(entity.getAmount())
                .reason(entity.getReason())
                .documentRef(entity.getDocumentRef())
                .build();
    }
}
