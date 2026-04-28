package com.crms.dto.request.quality;

import com.crms.domain.quality.enums.ScheduleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ITPScheduleRequest {

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    private Long templateId;

    private LocalDate startDate;

    private LocalDate dueDate;

    private ScheduleStatus status;

    private String assignedInspector;

    private String notes;

    private List<ScheduleItemRequest> items;

    @Data
    public static class ScheduleItemRequest {
        private Integer sequence;
        private String description;
        private com.crms.domain.quality.enums.InspectionType inspectionType;
        private String responsibleParty;
        private LocalDate dueDate;
        private String frequency;
        private String requiredEvidence;
    }
}
