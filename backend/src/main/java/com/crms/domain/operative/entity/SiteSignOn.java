package com.crms.domain.operative.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_sign_ons", indexes = {
    @Index(name = "idx_sso_site", columnList = "site_id"),
    @Index(name = "idx_sso_operative", columnList = "operative_id"),
    @Index(name = "idx_sso_sign_on_time", columnList = "sign_on_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSignOn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @Column(name = "sign_on_time", nullable = false)
    private LocalDateTime signOnTime;

    @Column(name = "sign_off_time")
    private LocalDateTime signOffTime;

    @Column(name = "plant_used")
    private String plantUsed;

    @Column(name = "plant_hours", precision = 5, scale = 2)
    private BigDecimal plantHours;

    @Column(name = "daywork_hours", precision = 5, scale = 2)
    private BigDecimal dayworkHours;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Embedded
    private LatLng location;

    @Column(name = "week_ending")
    private java.time.LocalDate weekEnding;

    public boolean isSignedOn() {
        return signOnTime != null && signOffTime == null;
    }

    public boolean isSignedOff() {
        return signOffTime != null;
    }

    public long getDurationMinutes() {
        if (signOnTime == null) return 0;
        LocalDateTime end = signOffTime != null ? signOffTime : LocalDateTime.now();
        return java.time.temporal.ChronoUnit.MINUTES.between(signOnTime, end);
    }
}
