package com.crms.dto.response.quality;

import com.crms.domain.quality.enums.InspectionResult;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InspectionRecordResponse {
    private Long id;
    private Long scheduleItemId;
    private String scheduleItemDescription;
    private Long scheduleId;
    private String scheduleTitle;
    private Long contractId;
    private String title;
    private String inspectorName;
    private String inspectorSignature;
    private LocalDate inspectionDate;
    private LocalDateTime inspectionTime;
    private InspectionResult result;
    private String notes;
    private String findings;
    private String nonConformanceRef;
    private List<AttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AttachmentResponse {
        private Long id;
        private String filename;
        private String fileType;
        private String filePath;
        private Long fileSize;
        private String description;
        private String uploadedBy;
    }
}
