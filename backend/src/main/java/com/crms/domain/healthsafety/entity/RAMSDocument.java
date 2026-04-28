package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.healthsafety.enums.RamsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "rams_documents", indexes = {
    @Index(name = "idx_rams_contract", columnList = "contract_id"),
    @Index(name = "idx_rams_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RAMSDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private RAMSTemplate template;

    @Column(nullable = false)
    private String title;

    @Column
    private String version;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RamsStatus status = RamsStatus.DRAFT;

    @Column(name = "document_ref")
    private String documentRef;

    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return validFrom != null && !validFrom.isAfter(now)
                && (validUntil == null || !validUntil.isBefore(now))
                && status == RamsStatus.SIGNED;
    }

    public boolean isExpiringSoon(int days) {
        if (validUntil == null) return false;
        return validUntil.isBefore(LocalDate.now().plusDays(days)) && !isExpired();
    }

    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDate.now());
    }
}
