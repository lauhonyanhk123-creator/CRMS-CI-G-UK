package com.crms.domain.tender.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.site.entity.Site;
import com.crms.domain.tender.enums.ContractForm;
import com.crms.domain.tender.enums.LossReason;
import com.crms.domain.tender.enums.MeasurementStandard;
import com.crms.domain.tender.enums.TenderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenders", indexes = {
    @Index(name = "idx_tender_ref", columnList = "tender_ref"),
    @Index(name = "idx_tender_status", columnList = "status"),
    @Index(name = "idx_tender_client", columnList = "client_id"),
    @Index(name = "idx_tender_site", columnList = "site_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tender extends BaseEntity {

    @Column(name = "tender_ref", nullable = false, unique = true)
    private String tenderRef;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Company client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenderStatus status = TenderStatus.LEAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_form")
    private ContractForm contractForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_standard")
    private MeasurementStandard measurementStandard;

    @Column(name = "value_range", precision = 12, scale = 2)
    private BigDecimal valueRange;

    @Column(name = "win_probability")
    private Integer winProbability;

    @Column(name = "tender_owner")
    private String tenderOwner;

    @Column(name = "tender_issued_date")
    private LocalDate tenderIssuedDate;

    @Column(name = "tender_return_date")
    private LocalDate tenderReturnDate;

    @Column(name = "tender_value_submitted", precision = 12, scale = 2)
    private BigDecimal tenderValueSubmitted;

    @Enumerated(EnumType.STRING)
    @Column(name = "loss_reason")
    private LossReason lossReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL)
    @Builder.Default
    private List<BoQItem> boqItems = new ArrayList<>();

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TenderDocument> tenderDocuments = new ArrayList<>();

    @OneToOne(mappedBy = "tender")
    private Contract contract;

    public boolean isWon() {
        return status == TenderStatus.AWARDED && contract != null;
    }

    public boolean isLost() {
        return status == TenderStatus.LOST;
    }
}
