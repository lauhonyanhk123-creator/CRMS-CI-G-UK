package com.crms.dto.request;

import com.crms.domain.adoption.enums.SnaggingItemPriority;
import com.crms.domain.adoption.enums.SnaggingItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnaggingItemRequest {
    
    @NotNull(message = "Adoption case ID is required")
    private Long adoptionCaseId;
    
    @NotNull(message = "Item description is required")
    private String description;
    
    private String location;
    
    private SnaggingItemPriority priority;
    
    private LocalDate identifiedDate;
    
    private LocalDate targetCompletionDate;
    
    private LocalDate actualCompletionDate;
    
    private SnaggingItemStatus status;
    
    private String notes;
    
    private String assignedTo;
}
