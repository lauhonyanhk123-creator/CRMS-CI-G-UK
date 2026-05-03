package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pay_less_notices", indexes = {
    @Index(name = "idx_pln_application", columnList = "application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayLessNotice extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationForPayment application;

    @Column(name = "issued_on", nullable = false)
    private LocalDateTime issuedOn;

    @Column(name = "notice_date")
    private java.time.LocalDate noticeDate;

    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

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

    @Column(name = "audit_log_id")
    private String auditLogId;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PayLessNoticeBuilder {
        public PayLessNoticeBuilder id(Long id) {
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
