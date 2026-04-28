package com.crms.domain.financial.repository;

import com.crms.domain.financial.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    Page<JournalEntry> findByJournalReference(String journalReference, Pageable pageable);

    List<JournalEntry> findByWipReportId(Long wipReportId);

    @Query("SELECT j FROM JournalEntry j WHERE j.transactionDate BETWEEN :startDate AND :endDate ORDER BY j.transactionDate")
    List<JournalEntry> findByTransactionDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT j FROM JournalEntry j WHERE j.journalType = :type AND j.status = 'POSTED'")
    List<JournalEntry> findPostedByType(@Param("type") String journalType);

    @Query("SELECT COALESCE(SUM(j.debitAmount), 0) FROM JournalEntry j WHERE j.journalReference = :ref")
    BigDecimal sumDebitsByJournalReference(@Param("ref") String journalReference);

    @Query("SELECT COALESCE(SUM(j.creditAmount), 0) FROM JournalEntry j WHERE j.journalReference = :ref")
    BigDecimal sumCreditsByJournalReference(@Param("ref") String journalReference);
}
