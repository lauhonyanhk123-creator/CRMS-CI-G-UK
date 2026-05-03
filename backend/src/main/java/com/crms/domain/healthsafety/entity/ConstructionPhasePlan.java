package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.healthsafety.enums.CppStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "construction_phase_plans", indexes = {
    @Index(name = "idx_cpp_contract", columnList = "contract_id"),
    @Index(name = "idx_cpp_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstructionPhasePlan extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "plan_ref")
    private String planRef;

    @Column
    private String title;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CppStatus status = CppStatus.DRAFT;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "document_ref")
    private String documentRef;

    public boolean isApproved() {
        return status == CppStatus.APPROVED || status == CppStatus.PUBLISHED;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class ConstructionPhasePlanBuilder {
        public ConstructionPhasePlanBuilder id(Long id) {
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
