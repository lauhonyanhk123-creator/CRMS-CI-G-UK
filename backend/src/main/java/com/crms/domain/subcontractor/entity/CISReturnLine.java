package com.crms.domain.subcontractor.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @Transient
    private Long compatibilityId;

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
    public void calculateDeduction() {
        if (grossPaid == null) {
            this.deduction = BigDecimal.ZERO.setScale(2);
            this.netPaid = null;
            return;
        }
        if (grossPaid != null && cisRate != null && cisRate.compareTo(BigDecimal.ZERO) > 0) {
            // CIS deduction = gross * (rate / 100), rates must be 0, 20, or 30 per HMRC rules
            this.deduction = grossPaid.multiply(cisRate)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            this.netPaid = grossPaid.subtract(this.deduction);
        } else if (grossPaid != null) {
            this.deduction = BigDecimal.ZERO.setScale(2);
            this.netPaid = grossPaid;
        }
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CISReturnLineBuilder {
        public CISReturnLineBuilder id(Long id) {
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
