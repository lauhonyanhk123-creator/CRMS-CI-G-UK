package com.crms.domain.plant.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hire_records", indexes = {
    @Index(name = "idx_hr_plant", columnList = "plant_id"),
    @Index(name = "idx_hr_supplier", columnList = "supplier_id"),
    @Index(name = "idx_hr_on_hire", columnList = "on_hire_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HireRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantItem plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    @Column(name = "on_hire_date", nullable = false)
    private LocalDate onHireDate;

    @Column(name = "off_hire_date")
    private LocalDate offHireDate;

    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isOnHire() {
        return offHireDate == null;
    }

    public long getHireDays() {
        if (onHireDate == null) return 0;
        LocalDate end = offHireDate != null ? offHireDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(onHireDate, end);
    }
}
