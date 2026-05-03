package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rams_sign_ons", indexes = {
    @Index(name = "idx_rams_sign_rams", columnList = "rams_id"),
    @Index(name = "idx_rams_sign_operative", columnList = "operative_id"),
    @Index(name = "idx_rams_sign_site", columnList = "site_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RAMSSignOn extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rams_id", nullable = false)
    private RAMSDocument rams;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean isValid() {
        return validUntil == null || validUntil.isAfter(LocalDateTime.now());
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class RAMSSignOnBuilder {
        public RAMSSignOnBuilder id(Long id) {
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
