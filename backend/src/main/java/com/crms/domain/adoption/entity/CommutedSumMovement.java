package com.crms.domain.adoption.entity;

import com.crms.domain.adoption.enums.CommutedSumType;
import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "commuted_sum_movements", indexes = {
    @Index(name = "idx_csm_case", columnList = "adoption_case_id"),
    @Index(name = "idx_csm_date", columnList = "movement_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommutedSumMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_case_id", nullable = false)
    private AdoptionCase adoptionCase;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommutedSumType type;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column
    private String reason;

    @Column(name = "document_ref")
    private String documentRef;
}
