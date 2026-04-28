package com.crms.dto.response.quality;

import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.TemplateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ITPTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String tradeCategory;
    private Integer version;
    private TemplateStatus status;
    private List<ITPItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    @Data
    @Builder
    public static class ITPItemResponse {
        private Long id;
        private Integer sequence;
        private String description;
        private InspectionType inspectionType;
        private String responsibleParty;
        private String notes;
        private String frequency;
        private String requiredEvidence;
    }
}
