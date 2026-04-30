package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "retention_ledger", indexes = {
    @Index(name = "idx_rl_contract", columnList = "contract_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetentionLedger extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    @Column(name = "total_retention", precision = 14, scale = 2)
    private BigDecimal totalRetention;

    @Column(name = "released_at_pc", precision = 14, scale = 2)
    private BigDecimal releasedAtPC;

    @Column(name = "released_at_defects", precision = 14, scale = 2)
    private BigDecimal releasedAtDefects;

    @Column(precision = 14, scale = 2)
    private BigDecimal balance;

    @OneToMany(mappedBy = "retentionLedger", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RetentionMovement> movements = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateBalance() {
        BigDecimal released = BigDecimal.ZERO;
        if (releasedAtPC != null) released = released.add(releasedAtPC);
        if (releasedAtDefects != null) released = released.add(releasedAtDefects);
        if (totalRetention != null) {
            this.balance = totalRetention.subtract(released);
        }
    }
}
