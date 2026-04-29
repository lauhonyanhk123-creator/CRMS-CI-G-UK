package com.crms.domain.operative.repository;

import com.crms.domain.operative.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByOperativeId(Long operativeId);

    List<Timesheet> findBySiteId(Long siteId);

    Optional<Timesheet> findByOperativeIdAndWeekEnding(Long operativeId, LocalDate weekEnding);

    @Query("SELECT t FROM Timesheet t WHERE t.site.id = :siteId AND t.weekEnding = :weekEnding")
    List<Timesheet> findBySiteAndWeekEnding(@Param("siteId") Long siteId, @Param("weekEnding") LocalDate weekEnding);

    @Query("SELECT t FROM Timesheet t WHERE t.exported = false AND t.weekEnding < :date")
    List<Timesheet> findUnExportedBeforeDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(t.regularHours + t.overtimeHours + t.holidayHours + t.sickHours) * o.hourlyRate, 0) " +
           "FROM Timesheet t JOIN t.operative o WHERE t.site.id = :siteId " +
           "AND t.weekEnding BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalWagesBySiteAndPeriod(@Param("siteId") Long siteId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.regularHours + t.overtimeHours + t.holidayHours + t.sickHours) * o.hourlyRate, 0) " +
           "FROM Timesheet t JOIN t.operative o WHERE t.site.id IN :siteIds " +
           "AND t.weekEnding BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalWagesBySitesAndPeriod(@Param("siteIds") List<Long> siteIds,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
