package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.enums.VariationStatus;
import com.crms.domain.contract.enums.VariationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "variations", indexes = {
    @Index(name = "idx_variation_contract", columnList = "contract_id"),
    @Index(name = "idx_variation_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variation extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "variation_ref", nullable = false, unique = true)
    private String variationRef;

    @Column(name = "instruction_ref")
    private String instructionRef;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(precision = 14, scale = 2)
    private java.math.BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariationType type;

    @Column(nullable = false)
    private String description;

    @Column(name = "original_value", precision = 12, scale = 2)
    private BigDecimal originalValue;

    @Column(name = "agreed_value", precision = 12, scale = 2)
    private BigDecimal agreedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VariationStatus status = VariationStatus.PROPOSED;

    @Column(name = "notified_date")
    private LocalDate notifiedDate;

    @Column(name = "agreed_date")
    private LocalDate agreedDate;

    @Column(name = "instructions_ref")
    private String instructionsRef;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class VariationBuilder {
        public VariationBuilder id(Long id) {
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
