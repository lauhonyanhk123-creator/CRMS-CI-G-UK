package com.crms.domain.adoption.entity;

import com.crms.domain.adoption.enums.AdoptionType;
import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.contract.entity.Contract;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adoption_cases", indexes = {
    @Index(name = "idx_ac_case_ref", columnList = "case_ref"),
    @Index(name = "idx_ac_status", columnList = "status"),
    @Index(name = "idx_ac_type", columnList = "adoption_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCase extends BaseEntity {

    @Column(name = "case_ref", nullable = false, unique = true)
    private String caseRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "adoption_type", nullable = false)
    private AdoptionType adoptionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Company client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "la_or_water_authority_id", nullable = false)
    private Company localAuthorityOrWaterAuthority;

    @Column(name = "technical_approval_ref")
    private String technicalApprovalRef;

    @Column(name = "design_check_fees", precision = 12, scale = 2)
    private BigDecimal designCheckFees;

    @Column(name = "supervision_fees", precision = 12, scale = 2)
    private BigDecimal supervisionFees;

    @Column(name = "commuted_sum_total", precision = 12, scale = 2)
    private BigDecimal commutedSumTotal;

    @Column(name = "commuted_sum_paid", precision = 12, scale = 2)
    private BigDecimal commutedSumPaid;

    @Column(name = "maintenance_period_months")
    private Integer maintenancePeriodMonths;

    @Column(name = "commencement_date")
    private LocalDate commencementDate;

    @Column(name = "maintenance_end_date")
    private LocalDate maintenanceEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private com.crms.domain.adoption.enums.AdoptionStatus status = com.crms.domain.adoption.enums.AdoptionStatus.PRE_APP;

    @OneToOne(mappedBy = "adoptionCase", cascade = CascadeType.ALL)
    private Bond bond;

    @OneToMany(mappedBy = "adoptionCase", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AdoptionStage> stages = new ArrayList<>();

    @OneToMany(mappedBy = "adoptionCase", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CommutedSumMovement> commutedSumMovements = new ArrayList<>();

    public BigDecimal getCommutedSumOutstanding() {
        if (commutedSumTotal == null) return BigDecimal.ZERO;
        BigDecimal paid = commutedSumPaid != null ? commutedSumPaid : BigDecimal.ZERO;
        return commutedSumTotal.subtract(paid);
    }
}
