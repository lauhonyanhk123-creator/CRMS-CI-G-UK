package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.RetentionMovement;
import com.crms.domain.contract.enums.RetentionMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RetentionMovementRepository extends JpaRepository<RetentionMovement, Long> {

    List<RetentionMovement> findByRetentionLedgerId(Long retentionLedgerId);

    @Query("SELECT SUM(m.amount) FROM RetentionMovement m WHERE m.retentionLedger.id = :ledgerId AND m.type = :type")
    Optional<BigDecimal> sumAmountByLedgerIdAndType(@Param("ledgerId") Long ledgerId, @Param("type") RetentionMovementType type);
}
