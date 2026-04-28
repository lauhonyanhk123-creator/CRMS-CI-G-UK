package com.crms.domain.adoption.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "commuted_sums", indexes = {
    @Index(name = "idx_cs_case", columnList = "adoption_case_id"),
    @Index(name = "idx_cs_type", columnList = "commuted_sum_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommutedSum extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_case_id", nullable = false)
    private AdoptionCase adoptionCase;

    @Column(name = "commuted_sum_type", nullable = false)
    private String commutedSumType;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "released_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal releasedAmount = BigDecimal.ZERO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public BigDecimal getOutstandingAmount() {
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return totalAmount.subtract(paid);
    }

    public boolean isFullyPaid() {
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return paid.compareTo(totalAmount) >= 0;
    }
}
