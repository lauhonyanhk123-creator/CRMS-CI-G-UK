package com.crms.domain.plant.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.plant.enums.PreUseCheckResult;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_pre_use_checks", indexes = {
    @Index(name = "idx_dpu_check_date", columnList = "check_date"),
    @Index(name = "idx_dpu_operative", columnList = "operative_id"),
    @Index(name = "idx_dpu_plant", columnList = "plant_id"),
    @Index(name = "idx_dpu_site", columnList = "site_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyPreUseCheck extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantItem plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreUseCheckResult result;

    @Column(name = "defects_noted", columnDefinition = "TEXT")
    private String defectsNoted;

    @Column(name = "repaired_before_use")
    @Builder.Default
    private Boolean repairedBeforeUse = false;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class DailyPreUseCheckBuilder {
        public DailyPreUseCheckBuilder id(Long id) {
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
