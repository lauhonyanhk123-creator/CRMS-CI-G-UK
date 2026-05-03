package com.crms.domain.subcontractor.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.subcontractor.enums.CisReturnStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cis_returns", indexes = {
    @Index(name = "idx_cis_return_tax_month", columnList = "tax_month"),
    @Index(name = "idx_cis_return_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CISReturn extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "tax_month", nullable = false)
    private String taxMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcontractor_id")
    private Subcontractor subcontractor;

    @Column(name = "submission_date")
    private java.time.LocalDate submissionDate;

    @Column(name = "gross_value", precision = 14, scale = 2)
    private java.math.BigDecimal grossValue;

    @Column(name = "deduction_amount", precision = 14, scale = 2)
    private java.math.BigDecimal deductionAmount;

    @Column(name = "total_gross_value", precision = 14, scale = 2)
    private java.math.BigDecimal totalGrossValue;

    @Column(name = "total_deduction", precision = 14, scale = 2)
    private java.math.BigDecimal totalDeduction;

    @Column(name = "total_deductions", precision = 14, scale = 2)
    private java.math.BigDecimal totalDeductions;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "hmrc_receipt_ref")
    private String hmrcReceiptRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CisReturnStatus status = CisReturnStatus.DRAFT;

    @OneToMany(mappedBy = "cisReturn", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CISReturnLine> cisReturnLines = new ArrayList<>();

    public boolean isDraft() {
        return status == CisReturnStatus.DRAFT;
    }

    public boolean isSubmitted() {
        return status == CisReturnStatus.SUBMITTED || status == CisReturnStatus.ACCEPTED;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CISReturnBuilder {
        public CISReturnBuilder id(Long id) {
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
