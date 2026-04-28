package com.crms.domain.financial.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cost_transactions", indexes = {
    @Index(name = "idx_ct_contract", columnList = "contract_id"),
    @Index(name = "idx_ct_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_ct_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "vendor_supplier")
    private String vendorSupplier;

    @Column(name = "reference")
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false)
    @Builder.Default
    private CostType costType = CostType.DIRECT;

    public enum CostType {
        DIRECT,
        INDIRECT,
        LABOUR,
        MATERIAL,
        SUBCONTRACTOR
    }
}
