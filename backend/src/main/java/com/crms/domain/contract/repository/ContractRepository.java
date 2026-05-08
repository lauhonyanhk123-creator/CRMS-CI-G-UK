package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ContractStatus;
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
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractRef(String contractRef);

    List<Contract> findByStatus(ContractStatus status);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.client.id = :clientId")
    List<Contract> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.site.id = :siteId")
    List<Contract> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.defectsEndDate <= :date AND c.status = 'COMPLETED'")
    List<Contract> findDefectsPeriodEnding(@Param("date") LocalDate date);

    boolean existsByContractRef(String contractRef);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.site.id = :siteId")
    Page<Contract> findBySiteId(@Param("siteId") Long siteId, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.status = :status")
    Page<Contract> findByStatus(@Param("status") ContractStatus status, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.client.id = :clientId")
    Page<Contract> findByClientId(@Param("clientId") Long clientId, Pageable pageable);
    
    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site")
    Page<Contract> findAll(Pageable pageable);
    long countByStatus(ContractStatus status);

    @Query("SELECT COALESCE(SUM(c.contractValue), 0) FROM Contract c WHERE c.status = :status")
    java.math.BigDecimal sumContractValueByStatus(@Param("status") ContractStatus status);

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.retentionLedger IS NOT NULL")
    List<Contract> findByRetentionLedgerIsNotNull();

    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.client LEFT JOIN FETCH c.site WHERE c.startDate <= :periodEnd AND (c.actualCompletionDate IS NULL OR c.actualCompletionDate >= :periodStart)")
    List<Contract> findContractsActiveDuring(@Param("periodStart") LocalDate periodStart, @Param("periodEnd") LocalDate periodEnd);
}
