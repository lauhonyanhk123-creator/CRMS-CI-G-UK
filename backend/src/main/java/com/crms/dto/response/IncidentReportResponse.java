package com.crms.dto.response;

import com.crms.domain.healthsafety.enums.IncidentStatus;
import com.crms.domain.healthsafety.enums.IncidentType;
import com.crms.domain.healthsafety.enums.Severity;
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
public class IncidentReportResponse {

    private Long id;
    private Long siteId;
    private String siteName;
    private Long operativeId;
    private String operativeName;
    private String reportNumber;
    private LocalDateTime incidentDate;
    private String locationDescription;
    private IncidentType type;
    private Severity severity;
    private String description;
    private String immediateActions;
    private Boolean ridDORNotifiable;
    private Boolean reportedToHse;
    private String hseRef;
    private String investigationOutcome;
    private List<String> documentRefs;
    private IncidentStatus status;

    // MOR fields
    private String morReference;
    private LocalDateTime morSubmittedDate;
    private String morSignedBy;
    private LocalDateTime morSignedDate;
    private String morVerificationStatus;
    private String morVerifiedBy;
    private LocalDateTime morVerifiedDate;
    private String morConditions;
    private String morRestrictions;
    private String morPermitRef;
    private LocalDateTime morPermitExpiry;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
