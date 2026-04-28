package com.crms.dto.response.quality;

import com.crms.domain.quality.enums.DefectPriority;
import com.crms.domain.quality.enums.DefectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DefectResponse {
    private Long id;
    private String title;
    private Long contractId;
    private String contractRef;
    private String description;
    private String location;
    private DefectPriority priority;
    private DefectStatus status;
    private LocalDate identifiedDate;
    private LocalDate dueDate;
    private LocalDate resolvedDate;
    private String assignedOperative;
    private String assignedContractor;
    private String notes;
    private String rootCause;
    private String resolutionDetails;
    private Boolean reinspectionRequired;
    private LocalDate reinspectionDate;
    private String ncReference;
    private List<PhotoResponse> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class PhotoResponse {
        private Long id;
        private String filename;
        private String filePath;
        private Long fileSize;
        private String description;
        private String uploadedBy;
        private String takenDate;
    }
}
