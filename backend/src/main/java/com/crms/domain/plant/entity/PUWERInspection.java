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

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantItem plant;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column
    private String inspector;

    @Column(name = "inspector_company")
    private String inspectorCompany;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "document_ref")
    private String documentRef;

    @Column(name = "report_ref")
    private String reportRef;
    public boolean isDue() {
        return nextDueDate != null && !nextDueDate.isAfter(java.time.LocalDate.now());
    }

    public boolean isDueSoon(int days) {
        return nextDueDate != null && !nextDueDate.isAfter(java.time.LocalDate.now().plusDays(days));
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PUWERInspectionBuilder {
        public PUWERInspectionBuilder id(Long id) {
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
