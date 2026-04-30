package com.crms.dto.request;

import com.crms.domain.plant.enums.InspectionResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PUWERRequest {

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    @NotNull(message = "Next due date is required")
    private LocalDate nextDueDate;

    private String inspector;

    private String inspectorCompany;

    @NotNull(message = "Result is required")
    private InspectionResult result;

    private String reportRef;

    private String notes;

    private String documentRef;
}