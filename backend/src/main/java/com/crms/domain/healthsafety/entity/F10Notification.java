package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "f10_notifications", indexes = {
    @Index(name = "idx_f10_contract", columnList = "contract_id"),
    @Index(name = "idx_f10_notification_number", columnList = "notification_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class F10Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "notification_number", nullable = false, unique = true)
    private String notificationNumber;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @Column(name = "confirmation_number")
    private String confirmationNumber;

    @Column(name = "more_than_30_days")
    private Boolean moreThan30Days;

    @Column(name = "more_than_500_person_days")
    private Boolean moreThan500PersonDays;

    @Column(name = "construction_start_date")
    private LocalDate constructionStartDate;

    @Column(name = "construction_end_date")
    private LocalDate constructionEndDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "hdf_reference")
    private String hdfReference;

    @Column(name = "hdf_submitted_date")
    private LocalDate hdfSubmittedDate;

    @Column(name = "hdf_acknowledged")
    @Builder.Default
    private Boolean hdfAcknowledged = false;

    @Column(name = "hdf_acknowledged_by")
    private String hdfAcknowledgedBy;

    @Column(name = "hdf_acknowledged_date")
    private LocalDateTime hdfAcknowledgedDate;

    public boolean isValid() {
        if (constructionEndDate != null && constructionEndDate.isBefore(LocalDate.now())) {
            return false;
        }
        return isActive != null && isActive;
    }

    public boolean requiresHDF() {
        return (moreThan30Days != null && moreThan30Days) || 
               (moreThan500PersonDays != null && moreThan500PersonDays);
    }
}
