package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.site.entity.Site;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.ContractForm;
import com.crms.domain.tender.enums.MeasurementStandard;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contract_ref", columnList = "contract_ref"),
    @Index(name = "idx_contract_status", columnList = "status"),
    @Index(name = "idx_contract_client", columnList = "client_id"),
    @Index(name = "idx_contract_site", columnList = "site_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends BaseEntity {

    @Column(name = "contract_ref", nullable = false, unique = true)
    private String contractRef;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Company client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tender_id")
    private Tender tender;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_form")
    private ContractForm contractForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_standard")
    private MeasurementStandard measurementStandard;

    @Column(name = "contract_value", precision = 14, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "retention_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal retentionPercent = new BigDecimal("5.0");

    @Column(name = "retention_reduction_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal retentionReductionPercent = new BigDecimal("2.5");

    @Column(name = "practical_completion_defects_period_months")
    @Builder.Default
    private Integer practicalCompletionDefectsPeriodMonths = 12;

    @Column(name = "payment_terms_days")
    @Builder.Default
    private Integer paymentTermsDays = 30;

    @Column(name = "final_date_for_payment_offset_days")
    @Builder.Default
    private Integer finalDateForPaymentOffsetDays = 14;

    @Column(name = "pay_less_notice_prescribed_period_days")
    @Builder.Default
    private Integer payLessNoticePrescribedPeriodDays = 7;

    @Column(name = "bond_percent", precision = 5, scale = 2)
    private BigDecimal bondPercent;

    @Column(name = "bond_value", precision = 12, scale = 2)
    private BigDecimal bondValue;

    @Column(name = "bond_ref")
    private String bondRef;

    @Column(name = "contract_documents")
    private String contractDocuments;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "defects_end_date")
    private LocalDate defectsEndDate;

    @Column(name = "nec4_options", columnDefinition = "TEXT")
    private String nec4Options;

    @Column(name = "nec4_pricing_mechanism")
    private String nec4PricingMechanism;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Variation> variations = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ApplicationForPayment> applicationsForPayment = new ArrayList<>();

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL)
    private RetentionLedger retentionLedger;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<com.crms.domain.adoption.entity.AdoptionCase> adoptionCases = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<com.crms.domain.healthsafety.entity.RAMSDocument> ramsDocuments = new ArrayList<>();

    public BigDecimal calculateRetention(BigDecimal value) {
        if (retentionPercent == null) return BigDecimal.ZERO;
        return value.multiply(retentionPercent).divide(new BigDecimal("100"));
    }

    public LocalDate calculateDefectsEndDate() {
        if (startDate != null && practicalCompletionDefectsPeriodMonths != null) {
            return startDate.plusMonths(practicalCompletionDefectsPeriodMonths);
        }
        return defectsEndDate;
    }
}
