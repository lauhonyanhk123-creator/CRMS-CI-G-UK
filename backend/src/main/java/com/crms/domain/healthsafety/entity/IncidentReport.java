package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.healthsafety.enums.IncidentStatus;
import com.crms.domain.healthsafety.enums.IncidentType;
import com.crms.domain.healthsafety.enums.Severity;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "incident_reports", indexes = {
    @Index(name = "idx_ir_site", columnList = "site_id"),
    @Index(name = "idx_ir_operative", columnList = "operative_id"),
    @Index(name = "idx_ir_report_number", columnList = "report_number"),
    @Index(name = "idx_ir_type", columnList = "type"),
    @Index(name = "idx_ir_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id")
    private Operative operative;

    @Column(name = "report_number", nullable = false, unique = true)
    private String reportNumber;

    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    @Column(name = "location_description")
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "immediate_actions", columnDefinition = "TEXT")
    private String immediateActions;

    @Column(name = "rid_dor_notifiable")
    private Boolean ridDORNotifiable;

    @Column(name = "reported_to_hse")
    private Boolean reportedToHse;

    @Column(name = "hse_ref")
    private String hseRef;

    @Column(name = "investigation_outcome", columnDefinition = "TEXT")
    private String investigationOutcome;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_refs", columnDefinition = "jsonb")
    private List<String> documentRefs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.DRAFT;

    // Method of Record (MOR) fields
    @Column(name = "mor_reference")
    private String morReference;

    @Column(name = "mor_submitted_date")
    private LocalDateTime morSubmittedDate;

    @Column(name = "mor_signed_by")
    private String morSignedBy;

    @Column(name = "mor_signed_date")
    private LocalDateTime morSignedDate;

    @Column(name = "mor_verification_status")
    private String morVerificationStatus;

    @Column(name = "mor_verified_by")
    private String morVerifiedBy;

    @Column(name = "mor_verified_date")
    private LocalDateTime morVerifiedDate;

    @Column(name = "mor_conditions", columnDefinition = "TEXT")
    private String morConditions;

    @Column(name = "mor_restrictions", columnDefinition = "TEXT")
    private String morRestrictions;

    @Column(name = "mor_permit_ref")
    private String morPermitRef;

    @Column(name = "mor_permit_expiry")
    private LocalDateTime morPermitExpiry;

    public boolean requiresRIDDOR() {
        return ridDORNotifiable != null && ridDORNotifiable;
    }

    public boolean isReportable() {
        return type == IncidentType.MAJOR_INJURY || type == IncidentType.FATALITY || requiresRIDDOR();
    }
}
