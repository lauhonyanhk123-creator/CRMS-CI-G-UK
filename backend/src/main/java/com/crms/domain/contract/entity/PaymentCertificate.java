package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_certificates", indexes = {
    @Index(name = "idx_pc_application", columnList = "application_id"),
    @Index(name = "idx_pc_certificate_number", columnList = "certificate_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCertificate extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationForPayment application;

    @Column(name = "certificate_number", nullable = false, unique = true)
    private String certificateNumber;

    @Column(name = "gross_certified", precision = 14, scale = 2)
    private BigDecimal grossCertified;

    @Column(name = "retention_held", precision = 14, scale = 2)
    private BigDecimal retentionHeld;

    @Column(name = "net_certified", precision = 14, scale = 2)
    private BigDecimal netCertified;

    @Column(name = "certified_date")
    private LocalDate certifiedDate;

    @Column(name = "certificate_ref")
    private String certificateRef;
}
