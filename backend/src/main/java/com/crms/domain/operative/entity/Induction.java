package com.crms.domain.operative.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.operative.enums.InductionMethod;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inductions", indexes = {
    @Index(name = "idx_induction_operative", columnList = "operative_id"),
    @Index(name = "idx_induction_site", columnList = "site_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Induction extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "inducted_at", nullable = false)
    private LocalDateTime inductedAt;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column
    private String trainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InductionMethod method = InductionMethod.SITE_VISIT;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isValid() {
        return validUntil == null || validUntil.isAfter(LocalDateTime.now());
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class InductionBuilder {
        public InductionBuilder id(Long id) {
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
