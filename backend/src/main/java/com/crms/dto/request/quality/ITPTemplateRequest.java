package com.crms.dto.request.quality;

import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.TemplateStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ITPTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String tradeCategory;

    private TemplateStatus status;

    @NotEmpty(message = "At least one inspection item is required")
    @Valid
    private List<ITPItemRequest> items;

    @Data
    public static class ITPItemRequest {
        @NotNull(message = "Sequence is required")
        private Integer sequence;

        @NotBlank(message = "Description is required")
        private String description;

        @NotNull(message = "Inspection type is required")
        private InspectionType inspectionType;

        @NotBlank(message = "Responsible party is required")
        private String responsibleParty;

        private String notes;

        private String frequency;

        private String requiredEvidence;
    }
}
