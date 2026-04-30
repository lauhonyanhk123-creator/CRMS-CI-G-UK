package com.crms.dto.response;

import com.crms.domain.healthsafety.entity.IncidentReport;
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

    public static IncidentReportResponse fromEntity(IncidentReport entity) {
        return IncidentReportResponse.builder()
                .id(entity.getId())
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .operativeId(entity.getOperative() != null ? entity.getOperative().getId() : null)
                .operativeName(entity.getOperative() != null ? entity.getOperative().getName() : null)
                .reportNumber(entity.getReportNumber())
                .incidentDate(entity.getIncidentDate())
                .locationDescription(entity.getLocationDescription())
                .type(entity.getType())
                .severity(entity.getSeverity())
                .description(entity.getDescription())
                .immediateActions(entity.getImmediateActions())
                .ridDORNotifiable(entity.getRidDORNotifiable())
                .reportedToHse(entity.getReportedToHse())
                .hseRef(entity.getHseRef())
                .investigationOutcome(entity.getInvestigationOutcome())
                .documentRefs(entity.getDocumentRefs())
                .status(entity.getStatus())
                .morReference(entity.getMorReference())
                .morSubmittedDate(entity.getMorSubmittedDate())
                .morSignedBy(entity.getMorSignedBy())
                .morSignedDate(entity.getMorSignedDate())
                .morVerificationStatus(entity.getMorVerificationStatus())
                .morVerifiedBy(entity.getMorVerifiedBy())
                .morVerifiedDate(entity.getMorVerifiedDate())
                .morConditions(entity.getMorConditions())
                .morRestrictions(entity.getMorRestrictions())
                .morPermitRef(entity.getMorPermitRef())
                .morPermitExpiry(entity.getMorPermitExpiry())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
