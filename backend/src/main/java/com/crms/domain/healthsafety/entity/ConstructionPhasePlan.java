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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

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
}
