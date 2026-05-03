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

    @Transient
    private Long compatibilityId;

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

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CommutedSumBuilder {
        public CommutedSumBuilder id(Long id) {
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
