package com.crms.domain.financial.repository;

import com.crms.domain.financial.entity.CostTransaction;
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
public interface CostTransactionRepository extends JpaRepository<CostTransaction, Long> {

    Page<CostTransaction> findByContractId(Long contractId, Pageable pageable);

    @Query("SELECT c FROM CostTransaction c WHERE c.contract.id = :contractId AND c.transactionDate <= :endDate")
    List<CostTransaction> findByContractIdAndUpToDate(
            @Param("contractId") Long contractId,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM CostTransaction c WHERE c.contract.id = :contractId AND c.transactionDate BETWEEN :startDate AND :endDate")
    List<CostTransaction> findByContractIdAndPeriod(
            @Param("contractId") Long contractId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CostTransaction c WHERE c.contract.id = :contractId AND c.transactionDate <= :endDate")
    BigDecimal sumAmountByContractIdUpToDate(
            @Param("contractId") Long contractId,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CostTransaction c WHERE c.contract.id = :contractId AND c.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByContractIdAndPeriod(
            @Param("contractId") Long contractId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT c.category, COALESCE(SUM(c.amount), 0) FROM CostTransaction c WHERE c.contract.id = :contractId AND c.transactionDate <= :endDate GROUP BY c.category")
    List<Object[]> sumAmountByCategory(
            @Param("contractId") Long contractId,
            @Param("endDate") LocalDate endDate);
}
