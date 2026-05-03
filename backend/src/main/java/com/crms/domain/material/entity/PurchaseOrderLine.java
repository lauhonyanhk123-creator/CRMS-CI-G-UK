package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_lines", indexes = {
    @Index(name = "idx_pol_order", columnList = "order_id"),
    @Index(name = "idx_pol_material", columnList = "material_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderLine extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false)
    private String description;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column
    private String unit;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "net_value", precision = 15, scale = 2)
    private BigDecimal netValue;

    @PrePersist
    @PreUpdate
    public void calculateNetValue() {
        if (unitPrice != null && quantity != null) {
            this.netValue = unitPrice.multiply(quantity);
        }
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PurchaseOrderLineBuilder {
        public PurchaseOrderLineBuilder id(Long id) {
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
