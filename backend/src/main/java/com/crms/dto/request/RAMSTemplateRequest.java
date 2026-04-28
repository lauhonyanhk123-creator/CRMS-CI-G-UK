package com.crms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAMSTemplateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String trade;

    private String riskAssessment;

    private String methodStatement;

    private String ppeRequired;

    private Integer frequencyDays;

    private Boolean isActive;
}
