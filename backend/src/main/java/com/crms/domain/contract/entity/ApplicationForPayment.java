package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "payer_ref")
    private String payerRef;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PaymentNotice> paymentNotices = new ArrayList<>();

    @OneToOne(mappedBy = "application")
    private PayLessNotice payLessNotice;

    @OneToOne(mappedBy = "application")
    private PaymentCertificate paymentCertificate;

    @PrePersist
    @PreUpdate
    public void calculateGrossValue() {
        if (valueOfWorks != null && retention != null) {
            this.grossValue = valueOfWorks.subtract(retention);
        }
    }
}
