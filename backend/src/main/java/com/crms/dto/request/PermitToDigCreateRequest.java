package com.crms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermitToDigCreateRequest {

    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotBlank(message = "Permit number is required")
    private String permitNumber;

    @NotBlank(message = "Location description is required")
    private String locationDescription;

    private String specificArea;
    private String depth;

    @NotBlank(message = "Nature of excavation is required")
    private String natureOfExcavation;

    private String anticipatedDuration;
    private String emergencyContact;
    private String emergencyNumber;

    private String permitIssuerName;
    private String permitIssuerTitle;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String diggingProcedure;
    private String supportingInformation;
}
