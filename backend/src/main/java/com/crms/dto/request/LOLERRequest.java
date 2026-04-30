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
public class LOLERRequest {

    @NotNull(message = "Examination date is required")
    private LocalDate examinationDate;

    @NotNull(message = "Next due date is required")
    private LocalDate nextDueDate;

    private String examiner;

    private String examinerCompany;

    @NotNull(message = "Result is required")
    private InspectionResult result;

    private String reportRef;

    private String notes;

    private String documentRef;
}