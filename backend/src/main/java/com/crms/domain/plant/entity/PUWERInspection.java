package com.crms.domain.plant.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.plant.enums.InspectionResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "puwer_inspections", indexes = {
    @Index(name = "idx_puwer_plant", columnList = "plant_id"),
    @Index(name = "idx_puwer_next_due", columnList = "next_due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PUWERInspection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantItem plant;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column
    private String inspector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "document_ref")
    private String documentRef;
}
