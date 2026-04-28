package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "materials", indexes = {
    @Index(name = "idx_material_code", columnList = "material_code"),
    @Index(name = "idx_material_description", columnList = "description")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material extends BaseEntity {

    @Column(name = "material_code", nullable = false, unique = true)
    private String materialCode;

    @Column(nullable = false)
    private String description;

    @Column
    private String trade;

    @Column
    private String unit;

    @Column
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Company supplier;

    @Column(name = "standard_rate", precision = 12, scale = 2)
    private BigDecimal standardRate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean hasSupplier() {
        return supplier != null;
    }
}
