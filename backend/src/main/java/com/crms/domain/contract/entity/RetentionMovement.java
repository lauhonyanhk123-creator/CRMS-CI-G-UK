package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.enums.RetentionMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "retention_movements", indexes = {
    @Index(name = "idx_rm_ledger", columnList = "retention_ledger_id"),
    @Index(name = "idx_rm_date", columnList = "movement_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetentionMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retention_ledger_id", nullable = false)
    private RetentionLedger retentionLedger;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetentionMovementType type;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
