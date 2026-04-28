package com.crms.dto.response.quality;

import com.crms.domain.quality.enums.BuildingControlType;
import com.crms.domain.quality.enums.SignOffResult;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SignOffResponse {
    private Long id;
    private Long contractId;
    private String contractRef;
    private BuildingControlType buildingControlType;
    private String inspectionType;
    private String referenceNumber;
    private String inspectorName;
    private String inspectorEmail;
    private String inspectorPhone;
    private LocalDate inspectionDate;
    private LocalDate nextInspectionDate;
    private SignOffResult result;
    private String conditionsOrNotes;
    private String reportPath;
    private String reportNumber;
    private String signOffSignature;
    private LocalDate signOffDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
