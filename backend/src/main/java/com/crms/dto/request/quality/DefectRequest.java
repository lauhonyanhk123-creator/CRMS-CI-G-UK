package com.crms.dto.request.quality;

import com.crms.domain.quality.enums.DefectPriority;
import com.crms.domain.quality.enums.DefectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DefectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    private DefectPriority priority;

    private DefectStatus status;

    private LocalDate identifiedDate;

    private LocalDate dueDate;

    private String assignedOperative;

    private String assignedContractor;

    private String notes;

    private String rootCause;

    private String resolutionDetails;

    private Boolean reinspectionRequired;

    private LocalDate reinspectionDate;

    private String ncReference;
}
