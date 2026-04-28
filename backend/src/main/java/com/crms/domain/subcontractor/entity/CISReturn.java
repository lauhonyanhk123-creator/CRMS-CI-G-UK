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

    @Column(name = "tax_month", nullable = false)
    private String taxMonth;

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
}
