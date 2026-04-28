package com.crms.domain.financial.repository;

import com.crms.domain.financial.entity.WipReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WipReportRepository extends JpaRepository<WipReport, Long> {

    Page<WipReport> findByContractId(Long contractId, Pageable pageable);

    List<WipReport> findByContractIdOrderByReportDateDesc(Long contractId);

    Optional<WipReport> findByContractIdAndReportDate(Long contractId, LocalDate reportDate);

    @Query("SELECT w FROM WipReport w WHERE w.contract.id = :contractId AND w.reportDate BETWEEN :startDate AND :endDate")
    List<WipReport> findByContractIdAndPeriod(
            @Param("contractId") Long contractId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT w FROM WipReport w WHERE w.reportDate = :reportDate AND w.status = 'DRAFT'")
    List<WipReport> findDraftsByReportDate(@Param("reportDate") LocalDate reportDate);

    @Query("SELECT w FROM WipReport w WHERE w.journalReference = :journalRef")
    Optional<WipReport> findByJournalReference(@Param("journalRef") String journalReference);

    boolean existsByContractIdAndReportDate(Long contractId, LocalDate reportDate);
}
