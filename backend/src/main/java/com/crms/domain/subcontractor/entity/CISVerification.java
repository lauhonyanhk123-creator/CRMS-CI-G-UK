package com.crms.domain.subcontractor.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.subcontractor.enums.CisVerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "cis_verifications", indexes = {
    @Index(name = "idx_cis_verification_ref", columnList = "verification_ref"),
    @Index(name = "idx_cis_verification_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CISVerification extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "verification_ref", nullable = false, unique = true)
    private String verificationRef;

    @Column(precision = 5, scale = 2)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CisVerificationStatus status;

    @Column(name = "verified_at")
    private LocalDate verifiedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verification_data", columnDefinition = "jsonb")
    private Map<String, Object> verificationData;

    public boolean isValid() {
        return expiresAt == null || expiresAt.isAfter(LocalDate.now());
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CISVerificationBuilder {
        public CISVerificationBuilder id(Long id) {
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
