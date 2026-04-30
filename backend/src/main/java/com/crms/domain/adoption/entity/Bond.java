package com.crms.domain.adoption.entity;

import com.crms.domain.adoption.enums.BondStatus;
import com.crms.domain.adoption.enums.BondType;
import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bonds", 
    indexes = {
        @Index(name = "idx_bond_number", columnList = "bond_number"),
        @Index(name = "idx_bond_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bond_adoption_case", columnNames = "adoption_case_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bond extends BaseEntity {

    @Column(name = "bond_number", nullable = false, unique = true)
    private String bondNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "bond_type", nullable = false)
    private BondType bondType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuing_surety_id", nullable = false)
    private Company issuingSurety;

    @Column(name = "bond_value", precision = 12, scale = 2, nullable = false)
    private BigDecimal bondValue;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "release_conditions", columnDefinition = "TEXT")
    private String releaseConditions;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BondStatus status = BondStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_case_id", nullable = false)
    private AdoptionCase adoptionCase;

    public boolean isActive() {
        return status == BondStatus.ACTIVE && (releaseDate == null || releaseDate.isAfter(LocalDate.now()));
    }

    public boolean isExpiringSoon(int days) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(days)) && !isExpired();
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}
