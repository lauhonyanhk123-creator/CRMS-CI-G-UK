package com.crms.domain.tender.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "boq_items", indexes = {
    @Index(name = "idx_boq_tender", columnList = "tender_id"),
    @Index(name = "idx_boq_item_code", columnList = "item_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoQItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tender_id", nullable = false)
    private Tender tender;

    @Column(name = "item_code")
    private String itemCode;

    @Column(nullable = false)
    private String description;

    @Column
    private String trade;

    @Column
    private String unit;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "measured_quantity", precision = 12, scale = 3)
    private BigDecimal measuredQuantity;

    @Column(name = "unit_rate", precision = 12, scale = 2)
    private BigDecimal unitRate;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "composite_id")
    private UUID compositeId;

    @Column(name = "is_locked")
    @Builder.Default
    private Boolean isLocked = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (measuredQuantity != null && unitRate != null) {
            this.totalValue = measuredQuantity.multiply(unitRate);
        } else if (quantity != null && unitRate != null) {
            this.totalValue = quantity.multiply(unitRate);
        }
    }
}
