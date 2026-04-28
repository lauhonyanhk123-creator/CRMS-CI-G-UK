package com.crms.dto.request.quality;

import com.crms.domain.quality.enums.BuildingControlType;
import com.crms.domain.quality.enums.SignOffResult;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SignOffRequest {

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotNull(message = "Building control type is required")
    private BuildingControlType buildingControlType;

    @NotNull(message = "Inspection type is required")
    private String inspectionType;

    private String referenceNumber;

    private String inspectorName;

    private String inspectorEmail;

    private String inspectorPhone;

    @NotNull(message = "Inspection date is required")
    private LocalDate inspectionDate;

    private LocalDate nextInspectionDate;

    @NotNull(message = "Result is required")
    private SignOffResult result;

    private String conditionsOrNotes;

    private String reportPath;

    private String reportNumber;

    private String signOffSignature;

    private LocalDate signOffDate;

    private String notes;
}
