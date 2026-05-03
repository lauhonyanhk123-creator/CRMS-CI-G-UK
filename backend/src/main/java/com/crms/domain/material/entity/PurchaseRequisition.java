package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.material.enums.PurchaseRequisitionStatus;
import com.crms.domain.site.entity.Site;
import com.crms.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_requisitions", indexes = {
    @Index(name = "idx_pr_ref", columnList = "requisition_ref"),
    @Index(name = "idx_pr_status", columnList = "status"),
    @Index(name = "idx_pr_site", columnList = "site_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequisition extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "requisition_ref", nullable = false, unique = true)
    private String requisitionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseRequisitionStatus status = PurchaseRequisitionStatus.DRAFT;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseRequisitionLine> lines = new ArrayList<>();

    public void addLine(PurchaseRequisitionLine line) {
        lines.add(line);
        line.setRequisition(this);
    }

    public void removeLine(PurchaseRequisitionLine line) {
        lines.remove(line);
        line.setRequisition(null);
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class PurchaseRequisitionBuilder {
        public PurchaseRequisitionBuilder id(Long id) {
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
