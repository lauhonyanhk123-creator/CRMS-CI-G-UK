package com.crms.domain.adoption.repository;

import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.BondStatus;
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
public interface BondRepository extends JpaRepository<Bond, Long> {

    Optional<Bond> findByBondNumber(String bondNumber);

    List<Bond> findByStatus(BondStatus status);

    @Query("SELECT b FROM Bond b WHERE b.adoptionCase.id = :caseId")
    Optional<Bond> findByAdoptionCaseId(@Param("caseId") Long caseId);

    @Query("SELECT b FROM Bond b WHERE b.adoptionCase.contract.id = :contractId")
    List<Bond> findByContractId(@Param("contractId") Long contractId);

    @Query("SELECT b FROM Bond b WHERE b.expiryDate <= :date AND b.status = :status")
    List<Bond> findExpiringBonds(@Param("date") LocalDate date, @Param("status") BondStatus status);

    @Query("SELECT b FROM Bond b WHERE b.expiryDate < :date AND b.status = 'ACTIVE'")
    List<Bond> findExpiredActiveBonds(@Param("date") LocalDate date);

    @Query("SELECT b FROM Bond b WHERE b.expiryDate BETWEEN :startDate AND :endDate AND b.status = 'ACTIVE'")
    List<Bond> findBondsExpiringBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Bond b WHERE b.status = :status")
    Page<Bond> findByStatus(@Param("status") BondStatus status, Pageable pageable);

    @Query("SELECT b FROM Bond b WHERE b.issuingSurety.id = :suretyId")
    List<Bond> findByIssuingSuretyId(@Param("suretyId") Long suretyId);

    @Query("SELECT b FROM Bond b WHERE b.status IN ('ACTIVE', 'PARTIALLY_RELEASED') AND b.expiryDate BETWEEN :today AND :alertDate")
    List<Bond> findBondsNeedingExpiryAlert(
            @Param("today") LocalDate today, 
            @Param("alertDate") LocalDate alertDate);

    boolean existsByBondNumber(String bondNumber);
}
