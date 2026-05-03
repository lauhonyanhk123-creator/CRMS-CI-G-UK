package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cdm_register", indexes = {
    @Index(name = "idx_cdm_project", columnList = "project_id"),
    @Index(name = "idx_cdm_notification_number", columnList = "notification_number"),
    @Index(name = "idx_cdm_client", columnList = "client_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CDMRegister extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "notification_number", nullable = false, unique = true)
    private String notificationNumber;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "project_address", columnDefinition = "TEXT")
    private String projectAddress;

    @Column(name = "project_description", columnDefinition = "TEXT")
    private String projectDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Company client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(name = "principal_designer_name")
    private String principalDesignerName;

    @Column(name = "principal_designer_email")
    private String principalDesignerEmail;

    @Column(name = "principal_designer_phone")
    private String principalDesignerPhone;

    @Column(name = "principal_contractor_name")
    private String principalContractorName;

    @Column(name = "principal_contractor_email")
    private String principalContractorEmail;

    @Column(name = "principal_contractor_phone")
    private String principalContractorPhone;

    @Column(name = "notification_date")
    private LocalDate notificationDate;

    @Column(name = "construction_start_date")
    private LocalDate constructionStartDate;

    @Column(name = "construction_end_date")
    private LocalDate constructionEndDate;

    @Column(name = "is_notifiable")
    @Builder.Default
    private Boolean isNotifiable = false;

    @Column(name = "more_than_30_days")
    private Boolean moreThan30Days;

    @Column(name = "more_than_500_person_days")
    private Boolean moreThan500PersonDays;

    @Column(name = "hse_notification_ref")
    private String hseNotificationRef;

    @Column(name = "date_preconstruction_info_shared")
    private LocalDate datePreconstructionInfoShared;

    @Column(name = "construction_phase_plan_date")
    private LocalDate constructionPhasePlanDate;

    @Column(name = "health_safety_file_ref")
    private String healthSafetyFileRef;

    @Column(name = "health_safety_file_created_date")
    private LocalDateTime healthSafetyFileCreatedDate;

    @Column(name = "health_safety_file_completed_date")
    private LocalDateTime healthSafetyFileCompletedDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public boolean isValid() {
        if (constructionEndDate != null && constructionEndDate.isBefore(LocalDate.now())) {
            return false;
        }
        return isActive != null && isActive;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CDMRegisterBuilder {
        public CDMRegisterBuilder id(Long id) {
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
