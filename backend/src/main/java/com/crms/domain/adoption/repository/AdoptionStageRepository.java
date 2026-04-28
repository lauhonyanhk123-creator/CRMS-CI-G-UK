package com.crms.domain.adoption.repository;

import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.enums.StageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionStageRepository extends JpaRepository<AdoptionStage, Long> {

    List<AdoptionStage> findByAdoptionCaseIdOrderByStageOrderAsc(Long adoptionCaseId);

    Optional<AdoptionStage> findByAdoptionCaseIdAndStageOrder(Long adoptionCaseId, Integer stageOrder);

    List<AdoptionStage> findByStatus(StageStatus status);

    @Query("SELECT s FROM AdoptionStage s WHERE s.adoptionCase.id = :caseId AND s.status = :status")
    List<AdoptionStage> findByAdoptionCaseIdAndStatus(
            @Param("caseId") Long caseId, 
            @Param("status") StageStatus status);

    @Query("SELECT s FROM AdoptionStage s WHERE s.plannedDate <= :date AND s.status NOT IN ('COMPLETED', 'SKIPPED')")
    List<AdoptionStage> findOverdueStages(@Param("date") LocalDate date);

    @Query("SELECT s FROM AdoptionStage s WHERE s.adoptionCase.contract.id = :contractId ORDER BY s.stageOrder ASC")
    List<AdoptionStage> findByContractId(@Param("contractId") Long contractId);

    @Query("SELECT COUNT(s) FROM AdoptionStage s WHERE s.adoptionCase.id = :caseId")
    long countByAdoptionCaseId(@Param("caseId") Long caseId);

    @Query("SELECT COUNT(s) FROM AdoptionStage s WHERE s.adoptionCase.id = :caseId AND s.status = :status")
    long countByAdoptionCaseIdAndStatus(@Param("caseId") Long caseId, @Param("status") StageStatus status);
}
