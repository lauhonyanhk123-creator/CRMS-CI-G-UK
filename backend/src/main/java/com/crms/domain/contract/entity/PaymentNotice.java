package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.enums.NoticeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_notices", indexes = {
    @Index(name = "idx_pn_application", columnList = "application_id"),
    @Index(name = "idx_pn_audit_log", columnList = "audit_log_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentNotice extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationForPayment application;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false)
    private NoticeType noticeType;

    @Column(name = "notice_date")
    private java.time.LocalDate noticeDate;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference")
    private String reference;

    @Column(name = "issued_on", nullable = false)
    private LocalDateTime issuedOn;

    @Column(name = "sum_considered_due", precision = 14, scale = 2)
    private BigDecimal sumConsideredDue;

    @Column
    @Builder.Default
    private String currency = "GBP";

    @Column(name = "basis_of_calculation", columnDefinition = "TEXT")
    private String basisOfCalculation;

    @Column(name = "document_ref")
    private String documentRef;

    @Column
    private String sha256;

    @Column(name = "final_date_for_payment")
    private LocalDateTime finalDateForPayment;

    @Column(name = "deadline_for_pay_less_notice")
    private LocalDateTime deadlineForPayLessNotice;

    @Column(name = "audit_log_id")
    private String auditLogId;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PaymentNoticeBuilder {
        public PaymentNoticeBuilder id(Long id) {
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
