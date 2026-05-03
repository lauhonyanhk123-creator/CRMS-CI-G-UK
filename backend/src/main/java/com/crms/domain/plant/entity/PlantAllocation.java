package com.crms.domain.plant.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.plant.enums.AllocationStatus;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "plant_allocations", indexes = {
    @Index(name = "idx_pa_plant", columnList = "plant_id"),
    @Index(name = "idx_pa_operative", columnList = "operative_id"),
    @Index(name = "idx_pa_site", columnList = "site_id"),
    @Index(name = "idx_pa_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantAllocation extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantItem plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.PLANNED;

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == AllocationStatus.ACTIVE
                && (startDate == null || !startDate.isAfter(today))
                && (endDate == null || !endDate.isBefore(today));
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PlantAllocationBuilder {
        public PlantAllocationBuilder id(Long id) {
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
