package com.crms.repository;

import com.crms.domain.contract.entity.BCISIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for BCIS cost indices used in CVR calculations.
 * Supports quarterly index lookup with linear interpolation between quarters.
 */
@Repository
public interface BCISIndexRepository extends JpaRepository<BCISIndex, Long> {

    /**
     * Find the BCIS index for a specific month (no interpolation).
     */
    Optional<BCISIndex> findBySeriesAndYearAndMonth(int series, int year, int month);

    /**
     * Get the most recent BCIS index on or before a given year/month for a series.
     * Used for interpolation when exact month has no index (BCIS publishes quarterly).
     */
    @Query("SELECT b FROM BCISIndex b " +
           "WHERE b.series = :series " +
           "AND (b.year < :year OR (b.year = :year AND b.month <= :month)) " +
           "ORDER BY b.year DESC, b.month DESC " +
           "LIMIT 1")
    Optional<BCISIndex> getMostRecentOnOrBefore(
        @Param("series") int series,
        @Param("year") int year,
        @Param("month") int month
    );

    /**
     * Get the index value for a given series and date.
     * Returns the raw value — interpolation must be applied separately.
     */
    @Query("SELECT COALESCE(b.indexValue, b.materialsIndex) FROM BCISIndex b " +
           "WHERE b.series = :series AND b.year = :year AND b.month = :month")
    Optional<BigDecimal> getIndexValue(
        @Param("series") int series,
        @Param("year") int year,
        @Param("month") int month
    );
}