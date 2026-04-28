package com.crms.domain.subcontractor.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cis_return_lines", indexes = {
    @Index(name = "idx_cis_line_return", columnList = "cis_return_id"),
    @Index(name = "idx_cis_line_subcontractor", columnList = "subcontractor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CISReturnLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cis_return_id", nullable = false)
    private CISReturn cisReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcontractor_id", nullable = false)
    private Company subcontractor;

    @Column(name = "gross_paid", precision = 12, scale = 2)
    private BigDecimal grossPaid;

    @Column(precision = 12, scale = 2)
    private BigDecimal deduction;

    @Column(name = "net_paid", precision = 12, scale = 2)
    private BigDecimal netPaid;

    @Column(name = "cis_rate", precision = 5, scale = 2)
    private BigDecimal cisRate;

    @PrePersist
    @PreUpdate
    public void calculateNetPaid() {
        if (grossPaid != null && deduction != null) {
            this.netPaid = grossPaid.subtract(deduction);
        }
    }

    @PrePersist
    @PreUpdate
    public void calculateDeduction() {
        if (grossPaid != null && cisRate != null) {
            this.deduction = grossPaid.multiply(cisRate).divide(new BigDecimal("100"));
        }
    }
}
