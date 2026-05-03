package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.healthsafety.enums.PermitStatus;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "permits_to_dig", indexes = {
    @Index(name = "idx_ptd_site", columnList = "site_id"),
    @Index(name = "idx_ptd_permit_number", columnList = "permit_number"),
    @Index(name = "idx_ptd_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermitToDig extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "permit_number", nullable = false, unique = true)
    private String permitNumber;

    @Column(name = "location_description")
    private String locationDescription;

    @Column(name = "works_description", columnDefinition = "TEXT")
    private String worksDescription;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PermitStatus status = PermitStatus.DRAFT;

    @Column(name = "lsbud_reference")
    private String lsbudReference;

    @Column(name = "trial_hole_count")
    private Integer trialHoleCount;

    @Column(name = "trial_hole_photo_ref")
    private String trialHolePhotoRef;

    @Column(name = "cat_scan_ref")
    private String catScanRef;

    @Column(name = "cat_scan_date")
    private LocalDate catScanDate;

    @Column(name = "cat_scan_device_serial")
    private String catScanDeviceSerial;

    @Column(name = "cat_scan_last_calibration_date")
    private LocalDate catScanLastCalibrationDate;

    @Column(name = "supervisor_approval_ref")
    private String supervisorApprovalRef;

    @Column(name = "supervisor_approval_date")
    private LocalDate supervisorApprovalDate;

    @Column(name = "document_ref")
    private String documentRef;

    // Workflow tracking fields
    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @Column(name = "prechecked_by")
    private String precheckedBy;

    @Column(name = "prechecked_date")
    private LocalDate precheckedDate;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "completed_by")
    private String completedBy;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancelled_date")
    private LocalDate cancelledDate;

    @Column(name = "extension_count")
    @Builder.Default
    private Integer extensionCount = 0;

    @Column(name = "last_extension_date")
    private LocalDate lastExtensionDate;

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return status == PermitStatus.ISSUED || status == PermitStatus.IN_PROGRESS
                && !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PermitToDigBuilder {
        public PermitToDigBuilder id(Long id) {
            this.compatibilityId = id;
            return this;
        }
    }


    @Override
    public Long getId() {
        Long id = super.getId();
        return id != null ? id : compatibilityId;
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
        this.compatibilityId = id;
    }

}
