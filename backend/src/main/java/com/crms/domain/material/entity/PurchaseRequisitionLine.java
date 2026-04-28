package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_requisition_lines", indexes = {
    @Index(name = "idx_prl_requisition", columnList = "requisition_id"),
    @Index(name = "idx_prl_material", columnList = "material_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequisitionLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private PurchaseRequisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private String description;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column
    private String unit;

    @Column(name = "estimated_unit_price", precision = 12, scale = 2)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "estimated_total", precision = 15, scale = 2)
    private BigDecimal estimatedTotal;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (estimatedUnitPrice != null && quantity != null) {
            this.estimatedTotal = estimatedUnitPrice.multiply(quantity);
        }
    }
}
