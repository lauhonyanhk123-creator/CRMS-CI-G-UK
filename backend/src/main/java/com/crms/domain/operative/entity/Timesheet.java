package com.crms.domain.operative.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "timesheets", indexes = {
    @Index(name = "idx_ts_operative", columnList = "operative_id"),
    @Index(name = "idx_ts_site", columnList = "site_id"),
    @Index(name = "idx_ts_week_ending", columnList = "week_ending")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "week_ending", nullable = false)
    private LocalDate weekEnding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "regular_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal regularHours = BigDecimal.ZERO;

    @Column(name = "overtime_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "holiday_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal holidayHours = BigDecimal.ZERO;

    @Column(name = "sick_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal sickHours = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    @Builder.Default
    private Boolean exported = false;

    public BigDecimal getTotalHours() {
        BigDecimal total = BigDecimal.ZERO;
        if (regularHours != null) total = total.add(regularHours);
        if (overtimeHours != null) total = total.add(overtimeHours);
        if (holidayHours != null) total = total.add(holidayHours);
        if (sickHours != null) total = total.add(sickHours);
        return total;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class TimesheetBuilder {
        public TimesheetBuilder id(Long id) {
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
