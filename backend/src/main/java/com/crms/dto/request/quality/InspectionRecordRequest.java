package com.crms.dto.request.quality;

import com.crms.domain.quality.enums.InspectionResult;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InspectionRecordRequest {

    @NotNull(message = "Schedule item ID is required")
    private Long scheduleItemId;

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Inspector name is required")
    private String inspectorName;

    private String inspectorSignature;

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    private LocalDateTime inspectionTime;

    @NotNull(message = "Result is required")
    private InspectionResult result;

    private String notes;

    private String findings;

    private String nonConformanceRef;
}
