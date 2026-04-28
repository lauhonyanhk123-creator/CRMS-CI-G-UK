package com.crms.domain.operative.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.operative.enums.QualificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "qualifications", indexes = {
    @Index(name = "idx_qual_operative", columnList = "operative_id"),
    @Index(name = "idx_qual_type", columnList = "qualification_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qualification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification_type", nullable = false)
    private QualificationType qualificationType;

    @Column
    private String level;

    @Column(name = "awarding_body")
    private String awardingBody;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "achieved_date")
    private LocalDate achievedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isValid() {
        return expiryDate == null || expiryDate.isAfter(LocalDate.now());
    }

    public boolean isExpiringSoon(int days) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(days)) && !isExpired();
    }
}
