package com.crms.dto.request;

import com.crms.domain.healthsafety.enums.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreateRequest {

    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotBlank(message = "Incident title is required")
    private String title;

    @NotNull(message = "Incident type is required")
    private IncidentType incidentType;

    @NotNull(message = "Date and time of incident is required")
    private LocalDateTime dateTimeOfIncident;

    @NotBlank(message = "Location of incident is required")
    private String location;

    @NotBlank(message = "Description of incident is required")
    private String description;

    private String personsInvolved;
    private String witnesses;
    private String immediateActionsTaken;
    private String injuriesSustained;
    private String equipmentInvolved;
    private String environmentalDamage;

    private Boolean ridDORNotifiable = false;
    private Boolean nearMiss = false;
    private String severity;
    private String status;
}
