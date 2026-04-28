package com.crms.domain.adoption.entity;

import com.crms.domain.adoption.enums.StageStatus;
import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "adoption_stages", indexes = {
    @Index(name = "idx_adoption_stage_case", columnList = "adoption_case_id"),
    @Index(name = "idx_adoption_stage_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionStage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_case_id", nullable = false)
    private AdoptionCase adoptionCase;

    @Column(name = "stage_name", nullable = false)
    private String stageName;

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @Column(name = "planned_date")
    private LocalDate plannedDate;

    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StageStatus status = StageStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isOverdue() {
        return status != StageStatus.COMPLETED && plannedDate != null && plannedDate.isBefore(LocalDate.now());
    }
}
