package com.crms.domain.plant.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.plant.enums.InspectionResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "loler_examinations", indexes = {
    @Index(name = "idx_loler_plant", columnList = "plant_id"),
    @Index(name = "idx_loler_next_due", columnList = "next_due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LOLERExamination extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private PlantItem plant;

    @Column(name = "examination_date", nullable = false)
    private LocalDate examinationDate;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    @Column
    private String examiner;

    @Column(name = "examiner_company")
    private String examinerCompany;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult result;

    @Column(name = "report_ref")
    private String reportRef;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "document_ref")
    private String documentRef;

    public boolean isDue() {
        return nextDueDate != null && nextDueDate.isBefore(LocalDate.now());
    }

    public boolean isDueSoon(int days) {
        if (nextDueDate == null) return false;
        return nextDueDate.isBefore(LocalDate.now().plusDays(days)) && !isDue();
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class LOLERExaminationBuilder {
        public LOLERExaminationBuilder id(Long id) {
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
