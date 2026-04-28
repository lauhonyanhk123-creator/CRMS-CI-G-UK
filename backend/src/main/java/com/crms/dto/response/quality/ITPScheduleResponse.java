package com.crms.dto.response.quality;

import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ITPScheduleResponse {
    private Long id;
    private String title;
    private Long contractId;
    private String contractRef;
    private Long templateId;
    private String templateName;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completedDate;
    private ScheduleStatus status;
    private String assignedInspector;
    private String signOffBy;
    private LocalDate signOffDate;
    private String signOffSignature;
    private String notes;
    private List<ScheduleItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class ScheduleItemResponse {
        private Long id;
        private Integer sequence;
        private String description;
        private InspectionType inspectionType;
        private String responsibleParty;
        private LocalDate dueDate;
        private String frequency;
        private String requiredEvidence;
        private ScheduleStatus status;
        private LocalDate completedDate;
        private String completedBy;
        private String result;
        private String notes;
    }
}
