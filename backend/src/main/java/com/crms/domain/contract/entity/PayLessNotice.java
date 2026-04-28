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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationForPayment application;

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

    @Column(name = "audit_log_id")
    private String auditLogId;
}
