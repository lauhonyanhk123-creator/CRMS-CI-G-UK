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

    @Transient
    private Long compatibilityId;

    @Column(name = "bond_number", nullable = false, unique = true)
    private String bondNumber;

    @Column(name = "bond_ref")
    private String bondRef;

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

    @Column(name = "release_requested")
    @Builder.Default
    private Boolean releaseRequested = false;

    @Column(name = "release_requested_date")
    private LocalDate releaseRequestedDate;

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

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class BondBuilder {
        public BondBuilder id(Long id) {
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
