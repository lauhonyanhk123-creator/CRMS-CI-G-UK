package com.crms.domain.quality.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.quality.enums.DefectPriority;
import com.crms.domain.quality.enums.DefectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "defects", indexes = {
    @Index(name = "idx_defect_contract", columnList = "contract_id"),
    @Index(name = "idx_defect_status", columnList = "status"),
    @Index(name = "idx_defect_priority", columnList = "priority"),
    @Index(name = "idx_defect_location", columnList = "location")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defect extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DefectPriority priority = DefectPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DefectStatus status = DefectStatus.OPEN;

    @Column(name = "identified_date")
    private LocalDate identifiedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;

    @Column(name = "assigned_operative")
    private String assignedOperative;

    @Column(name = "assigned_contractor")
    private String assignedContractor;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(name = "reinspection_required")
    @Builder.Default
    private Boolean reinspectionRequired = false;

    @Column(name = "reinspection_date")
    private LocalDate reinspectionDate;

    @Column(name = "nc_reference")
    private String ncReference;

    @OneToMany(mappedBy = "defect", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DefectPhoto> photos = new ArrayList<>();

    public void addPhoto(DefectPhoto photo) {
        photos.add(photo);
        photo.setDefect(this);
    }

    public void removePhoto(DefectPhoto photo) {
        photos.remove(photo);
        photo.setDefect(null);
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class DefectBuilder {
        public DefectBuilder id(Long id) {
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
