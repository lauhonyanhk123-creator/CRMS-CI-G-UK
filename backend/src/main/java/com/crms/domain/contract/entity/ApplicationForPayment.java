package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.DeadlineStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications_for_payment", indexes = {
    @Index(name = "idx_afp_contract", columnList = "contract_id"),
    @Index(name = "idx_afp_application_number", columnList = "application_number"),
    @Index(name = "idx_afp_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationForPayment extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "application_ref", nullable = false, unique = true)
    private String applicationRef;

    @Column(name = "application_number", nullable = false)
    private Integer applicationNumber;

    @Column(name = "application_period_start", nullable = false)
    private LocalDate applicationPeriodStart;

    @Column(name = "application_period_end", nullable = false)
    private LocalDate applicationPeriodEnd;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "value_of_works", precision = 14, scale = 2)
    private BigDecimal valueOfWorks;

    @Column(precision = 14, scale = 2)
    private BigDecimal retention;

    @Column(name = "gross_value", precision = 14, scale = 2)
    private BigDecimal grossValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "payer_ref")
    private String payerRef;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pay_less_notice_id")
    private PayLessNotice payLessNotice;
    
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentNotice> paymentNotices = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayLessNotice> payLessNotices = new ArrayList<>();

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private PaymentCertificate paymentCertificate;

    @Column(name = "reverse_charge", nullable = false)
    @Builder.Default
    private Boolean reverseCharge = false;

    // ================================================================
    // CIS Gate - Construction Industry Scheme verification gate
    // ================================================================
    // CIS gate must pass before application can be submitted
    @Column(name = "cis_gate_passed", nullable = false)
    @Builder.Default
    private Boolean cisGatePassed = false;

    @Column(name = "cis_gate_checked_at")
    private LocalDateTime cisGateCheckedAt;

    @Column(name = "cis_gate_notes", columnDefinition = "TEXT")
    private String cisGateNotes;

    // ================================================================
    // VAT Trigger - Reverse charge VAT trigger field
    // ================================================================
    // Tracks whether reverse charge VAT applies to this application
    // Triggered when: client is VAT registered AND contract >= £85,000
    @Column(name = "vat_triggered", nullable = false)
    @Builder.Default
    private Boolean vatTriggered = false;

    @Column(name = "vat_trigger_reason", columnDefinition = "TEXT")
    private String vatTriggerReason;

    @Column(name = "vat_threshold", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal vatThreshold = new BigDecimal("85000.00");

    // ================================================================
    // S.111 Enforcement - Pay-less notice deadline tracking
    // Under s.111 of the Housing Grants, Construction and Regeneration Act 1996
    // ================================================================
    @Column(name = "pay_less_notice_deadline")
    private LocalDate payLessNoticeDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_status")
    private DeadlineStatus deadlineStatus;

    @Column(name = "pay_less_notice_served")
    @Builder.Default
    private Boolean payLessNoticeServed = false;

    @Column(name = "pay_less_notice_served_at")
    private LocalDateTime payLessNoticeServedAt;

    // ================================================================
    // Retention Release - Tracks retention release status
    // Retention is typically released at practical completion and defects end
    // ================================================================
    @Column(name = "retention_released", nullable = false)
    @Builder.Default
    private Boolean retentionReleased = false;

    @Column(name = "retention_released_at_pc", precision = 14, scale = 2)
    private BigDecimal retentionReleasedAtPC;

    @Column(name = "retention_released_at_defects", precision = 14, scale = 2)
    private BigDecimal retentionReleasedAtDefects;

    @Column(name = "retention_release_date")
    private LocalDate retentionReleaseDate;

    @Column(name = "retention_release_reason", columnDefinition = "TEXT")
    private String retentionReleaseReason;

    @PrePersist
    @PreUpdate
    public void onSave() {
        if (valueOfWorks != null && retention != null) {
            this.grossValue = valueOfWorks.subtract(retention);
        }
        // Deadline is 5 days before the submitted date (s.111 Housing Grants Act)
        if (this.submittedDate != null) {
            this.payLessNoticeDeadline = this.submittedDate.minusDays(5);
            this.deadlineStatus = calculateDeadlineStatus(this.payLessNoticeDeadline);
        }
    }

    /**
     * Calculate deadline status based on current date and deadline.
     * @param deadline the pay-less notice deadline
     * @return the calculated deadline status
     */
    private DeadlineStatus calculateDeadlineStatus(LocalDate deadline) {
        if (deadline == null) {
            return DeadlineStatus.NO_DEADLINE;
        }
        
        LocalDate today = LocalDate.now();
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, deadline);
        
        if (daysRemaining < 0) {
            return DeadlineStatus.DEADLINE_PASSED;
        } else if (daysRemaining <= 2) {
            return DeadlineStatus.DEADLINE_APPROACHING;
        } else {
            return DeadlineStatus.DEADLINE_ACTIVE;
        }
    }

    /**
     * Check if CIS gate is passed - required before submission.
     * @return true if CIS gate check passed
     */
    public boolean isCisGatePassable() {
        return Boolean.TRUE.equals(this.cisGatePassed);
    }

    /**
     * Check if retention can be released.
     * @param defectsEndDate the contract defects liability period end date
     * @return true if retention can be released
     */
    public boolean canReleaseRetention(LocalDate defectsEndDate) {
        if (Boolean.TRUE.equals(this.retentionReleased)) {
            return false;
        }
        if (defectsEndDate != null && LocalDate.now().isAfter(defectsEndDate)) {
            return true;
        }
        return false;
    }

    /**
     * Release retention at practical completion.
     * @param amount the amount to release
     */
    public void releaseRetentionAtPC(BigDecimal amount) {
        this.retentionReleasedAtPC = amount;
        this.retentionReleaseDate = LocalDate.now();
        this.retentionReleaseReason = "Released at practical completion per contract terms";
        checkRetentionFullyReleased();
    }

    /**
     * Release retention at defects end.
     * @param amount the amount to release
     */
    public void releaseRetentionAtDefects(BigDecimal amount) {
        this.retentionReleasedAtDefects = amount;
        this.retentionReleaseDate = LocalDate.now();
        this.retentionReleaseReason = "Released at defects liability end per contract terms";
        checkRetentionFullyReleased();
    }

    /**
     * Check if all retention has been released.
     */
    private void checkRetentionFullyReleased() {
        BigDecimal totalReleased = BigDecimal.ZERO;
        if (retentionReleasedAtPC != null) totalReleased = totalReleased.add(retentionReleasedAtPC);
        if (retentionReleasedAtDefects != null) totalReleased = totalReleased.add(retentionReleasedAtDefects);
        
        if (retention != null && totalReleased.compareTo(retention) >= 0) {
            this.retentionReleased = true;
        }
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class ApplicationForPaymentBuilder {
        public ApplicationForPaymentBuilder id(Long id) {
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
