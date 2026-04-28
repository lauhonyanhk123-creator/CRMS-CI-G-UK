package com.crms.dto.request;

import com.crms.domain.healthsafety.enums.IncidentType;
import com.crms.domain.healthsafety.enums.Severity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReportRequest {

    @NotNull(message = "Site ID is required")
    private Long siteId;

    private Long operativeId;

    @NotNull(message = "Incident date is required")
    private LocalDateTime incidentDate;

    private String locationDescription;

    @NotNull(message = "Incident type is required")
    private IncidentType type;

    @NotNull(message = "Severity is required")
    private Severity severity;

    @NotNull(message = "Description is required")
    private String description;

    private String immediateActions;

    private Boolean ridDORNotifiable;

    private Boolean reportedToHse;

    private String hseRef;

    private String investigationOutcome;

    private List<String> documentRefs;

    // MOR fields
    private String morReference;
    private String morConditions;
    private String morRestrictions;
    private String morPermitRef;
    private LocalDateTime morPermitExpiry;
}
