package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.healthsafety.enums.UtilityType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cat_scan_records", indexes = {
    @Index(name = "idx_csr_permit", columnList = "permit_id"),
    @Index(name = "idx_csr_scan_date", columnList = "scan_date"),
    @Index(name = "idx_csr_utility", columnList = "utility")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CATScanRecord extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permit_id", nullable = false)
    private PermitToDig permit;

    @Column(name = "scan_date", nullable = false)
    private LocalDateTime scanDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UtilityType utility;

    @Column(precision = 8, scale = 2)
    private BigDecimal depth;

    @Column(name = "marked_by")
    private String markedBy;

    @Column(name = "photo_ref")
    private String photoRef;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CATScanRecordBuilder {
        public CATScanRecordBuilder id(Long id) {
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
