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
public class CPPCreateRequest {

    @NotBlank(message = "Project title is required")
    private String title;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    private String documentRef;
    private String version;

    @NotBlank(message = "Site address is required")
    private String siteAddress;

    @NotBlank(message = "Site manager name is required")
    private String siteManagerName;

    private String siteManagerPhone;
    private String siteManagerEmail;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String scopeOfWork;
    private String hazardousActivities;
    private String emergencyProcedures;
    private String ppeRequirements;
    private String firstAidProvisions;
    private String environmentalControls;
    private String wasteManagement;
    private String trafficManagement;

    private Boolean isActive = true;
}
