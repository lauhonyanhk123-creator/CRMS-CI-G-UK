package com.crms.domain.adoption.repository;

import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.enums.AdoptionType;
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
public interface AdoptionCaseRepository extends JpaRepository<AdoptionCase, Long> {

    Optional<AdoptionCase> findByCaseRef(String caseRef);

    List<AdoptionCase> findByAdoptionType(AdoptionType type);

    Page<AdoptionCase> findByAdoptionType(AdoptionType type, Pageable pageable);

    List<AdoptionCase> findByStatus(AdoptionStatus status);

    Page<AdoptionCase> findByStatus(AdoptionStatus status, Pageable pageable);

    @Query("SELECT a FROM AdoptionCase a WHERE a.contract.id = :contractId")
    List<AdoptionCase> findByContractId(@Param("contractId") Long contractId);

    @Query("SELECT a FROM AdoptionCase a WHERE a.contract.id = :contractId")
    Page<AdoptionCase> findByContractId(@Param("contractId") Long contractId, Pageable pageable);

    @Query("SELECT a FROM AdoptionCase a WHERE a.client.id = :clientId")
    List<AdoptionCase> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT a FROM AdoptionCase a WHERE a.maintenanceEndDate <= :date AND a.status = 'MAINTENANCE'")
    List<AdoptionCase> findCasesNearingMaintenanceEnd(@Param("date") LocalDate date);

    @Query("SELECT a FROM AdoptionCase a WHERE a.maintenanceEndDate = :date AND a.status = 'MAINTENANCE'")
    List<AdoptionCase> findCasesWithMaintenanceEndingOn(@Param("date") LocalDate date);

    @Query("SELECT a FROM AdoptionCase a WHERE a.status = :status AND a.commencementDate <= :date")
    List<AdoptionCase> findByStatusAndCommencementDateBefore(
            @Param("status") AdoptionStatus status, @Param("date") LocalDate date);

    @Query("SELECT a FROM AdoptionCase a WHERE a.status = :status ORDER BY a.maintenanceEndDate ASC")
    List<AdoptionCase> findByStatusOrderByMaintenanceEndDate(@Param("status") AdoptionStatus status);

    boolean existsByCaseRef(String caseRef);

    @Query("SELECT COUNT(a) FROM AdoptionCase a WHERE a.contract.id = :contractId")
    long countByContractId(@Param("contractId") Long contractId);

    @Query("SELECT COUNT(a) FROM AdoptionCase a WHERE a.status = :status")
    long countByStatus(@Param("status") AdoptionStatus status);

    @Query("SELECT a FROM AdoptionCase a WHERE a.adoptionType = :type AND a.status = :status")
    List<AdoptionCase> findByAdoptionTypeAndStatus(
            @Param("type") AdoptionType type, @Param("status") AdoptionStatus status);
}
