package com.crms.domain.adoption.repository;

import java.util.Optional;
import com.crms.domain.adoption.entity.CommutedSumMovement;
import com.crms.domain.adoption.enums.CommutedSumType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommutedSumMovementRepository extends JpaRepository<CommutedSumMovement, Long> {

    List<CommutedSumMovement> findByAdoptionCaseId(Long adoptionCaseId);

    @Query("SELECT SUM(m.amount) FROM CommutedSumMovement m WHERE m.adoptionCase.id = :adoptionCaseId AND m.type = :type")
    Optional<BigDecimal> sumByAdoptionCaseIdAndType(@Param("adoptionCaseId") Long adoptionCaseId, @Param("type") CommutedSumType type);

    @Query("SELECT m FROM CommutedSumMovement m WHERE m.adoptionCase.id = :adoptionCaseId AND m.movementDate BETWEEN :start AND :end")
    List<CommutedSumMovement> findByAdoptionCaseIdAndDateRange(@Param("adoptionCaseId") Long adoptionCaseId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM CommutedSumMovement m")
    BigDecimal sumTotalAmount();
}
