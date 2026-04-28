package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.material.enums.PurchaseOrderStatus;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders", indexes = {
    @Index(name = "idx_po_ref", columnList = "purchase_order_ref"),
    @Index(name = "idx_po_order_number", columnList = "order_number"),
    @Index(name = "idx_po_status", columnList = "status"),
    @Index(name = "idx_po_supplier", columnList = "supplier_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder extends BaseEntity {

    @Column(name = "purchase_order_ref", nullable = false, unique = true)
    private String purchaseOrderRef;

    @Column(name = "order_number")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id")
    private PurchaseRequisition requisition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "net_value", precision = 15, scale = 2)
    private BigDecimal netValue;

    @Column(name = "vat_value", precision = 15, scale = 2)
    private BigDecimal vatValue;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderLine> lines = new ArrayList<>();

    public void addLine(PurchaseOrderLine line) {
        lines.add(line);
        line.setOrder(this);
    }

    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        BigDecimal net = lines.stream()
                .map(PurchaseOrderLine::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.netValue = net;
        this.vatValue = net.multiply(new BigDecimal("0.20"));
        this.totalValue = net.add(vatValue);
    }
}
