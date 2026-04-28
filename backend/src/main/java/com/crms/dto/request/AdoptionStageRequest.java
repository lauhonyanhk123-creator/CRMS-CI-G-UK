package com.crms.dto.request;

import com.crms.domain.adoption.enums.StageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionStageRequest {
    
    @NotNull(message = "Stage name is required")
    private String stageName;
    
    @NotNull(message = "Stage order is required")
    private Integer stageOrder;
    
    private LocalDate plannedDate;
    
    private LocalDate actualDate;
    
    private StageStatus status;
    
    private String notes;
}
