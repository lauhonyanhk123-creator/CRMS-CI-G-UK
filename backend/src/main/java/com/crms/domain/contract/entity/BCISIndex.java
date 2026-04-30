package com.crms.domain.contract.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BCIS (Building Cost Information Service) material cost indices.
 * Used for CESMM4 Schedule R indexation of materials costs in CVR reports.
 * Base indices are quarterly; interpolate between quarters for monthly valuations.
 * Source: https://www.bcis.co.uk/
 */
@Entity
@Table(name = "bcis_indices", 
    indexes = {
        @Index(name = "idx_bcis_year_month", columnList = "year, month"),
        @Index(name = "idx_bcis_series", columnList = "series"),
        @Index(name = "idx_bcis_series_year_month", columnList = "series, year, month", unique = true)
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bcis_series_year_month", columnNames = {"series", "year", "month"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BCISIndex extends BaseEntity {

    /**
     * BCIS series number.
     * Series 1 = All-in (materials + labour + plant)
     * Series 3 = Materials only
     * Series 4 = Labour
     * Series 5 = Plant
     */
    @Column(nullable = false)
    private Integer series;  // 1, 3, 4, 5

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;  // 1-12

    /**
     * The index value, expressed relative to a base year.
     * BCIS typically uses 2015 = 100, so values are like 142.3
     */
    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal indexValue;

    @Column(precision = 8, scale = 2)
    private BigDecimal materialsIndex;  // Series 3

    @Column(precision = 8, scale = 2)
    private BigDecimal allInIndex;       // Series 1

    @Column(precision = 8, scale = 2)
    private BigDecimal labourIndex;      // Series 4

    @Column(precision = 8, scale = 2)
    private BigDecimal plantIndex;       // Series 5

    /**
     * Interpolation factor (0.0–1.0) used when valuation date falls between
     * two quarterly index points. Stored at retrieval time to avoid recomputing.
     */
    @Column(precision = 4, scale = 2)
    private BigDecimal interpolationFactor;

    @Column(length = 500)
    private String source;
}