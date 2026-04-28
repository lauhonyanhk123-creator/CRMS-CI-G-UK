package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAMSTemplateResponse {

    private Long id;
    private String title;
    private String description;
    private String trade;
    private String riskAssessment;
    private String methodStatement;
    private String ppeRequired;
    private Integer frequencyDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
