package com.crms.domain.plant.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.plant.entity.DailyPreUseCheck;
import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.enums.HireStatus;
import com.crms.domain.plant.enums.PlantCategory;
import com.crms.domain.plant.enums.PlantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plant_items", indexes = {
    @Index(name = "idx_plant_ref", columnList = "plant_ref"),
    @Index(name = "idx_plant_status", columnList = "status"),
    @Index(name = "idx_plant_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantItem extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "plant_ref", nullable = false, unique = true)
    private String plantRef;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(nullable = false)
    private String description;

    @Column
    private String make;

    @Column
    private String model;

    @Column
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlantCategory category;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "hire_status", nullable = false)
    @Builder.Default
    private HireStatus hireStatus = HireStatus.OWNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Company supplier;

    @Column(name = "telematics_id")
    private String telematicsId;

    @Column(name = "quick_hitch_type")
    private String quickHitchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlantStatus status = PlantStatus.AVAILABLE;

    @Column(name = "daily_hire_rate", precision = 10, scale = 2)
    private BigDecimal dailyHireRate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LOLERExamination> lolerExaminations = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PUWERInspection> puwerInspections = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DailyPreUseCheck> dailyPreUseChecks = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HireRecord> hireRecords = new ArrayList<>();

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlantAllocation> plantAllocations = new ArrayList<>();

    public boolean isAvailable() {
        return status == PlantStatus.AVAILABLE;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PlantItemBuilder {
        public PlantItemBuilder id(Long id) {
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
