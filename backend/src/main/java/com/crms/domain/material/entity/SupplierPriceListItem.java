package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_price_list_items", indexes = {
    @Index(name = "idx_spl_material", columnList = "material_id"),
    @Index(name = "idx_spl_supplier", columnList = "supplier_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPriceListItem extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column
    private String unit;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return (validFrom == null || !validFrom.isAfter(now))
                && (validTo == null || !validTo.isBefore(now));
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class SupplierPriceListItemBuilder {
        public SupplierPriceListItemBuilder id(Long id) {
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
