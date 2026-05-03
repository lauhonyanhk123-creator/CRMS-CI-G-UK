package com.crms.domain.financial.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "wip_reports", indexes = {
    @Index(name = "idx_wip_contract", columnList = "contract_id"),
    @Index(name = "idx_wip_report_date", columnList = "report_date"),
    @Index(name = "idx_wip_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WipReport extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "certified_value", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal certifiedValue = BigDecimal.ZERO;

    @Column(name = "cost_incurred", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costIncurred = BigDecimal.ZERO;

    @Column(name = "wip_value", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal wipValue = BigDecimal.ZERO;

    @Column(name = "under_recovery", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal underRecovery = BigDecimal.ZERO;

    @Column(name = "over_recovery", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal overRecovery = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WipReportStatus status = WipReportStatus.DRAFT;

    @Column(name = "journal_reference")
    private String journalReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum WipReportStatus {
        DRAFT,
        POSTED,
        REVERSED
    }

    public void calculateWip() {
        this.wipValue = certifiedValue.subtract(costIncurred);
        if (wipValue.compareTo(BigDecimal.ZERO) > 0) {
            this.overRecovery = BigDecimal.ZERO;
            this.underRecovery = wipValue;
        } else if (wipValue.compareTo(BigDecimal.ZERO) < 0) {
            this.underRecovery = BigDecimal.ZERO;
            this.overRecovery = wipValue.abs();
        } else {
            this.underRecovery = BigDecimal.ZERO;
            this.overRecovery = BigDecimal.ZERO;
        }
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class WipReportBuilder {
        public WipReportBuilder id(Long id) {
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
