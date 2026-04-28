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

    @Query("SELECT c FROM Contract c WHERE c.client.id = :clientId")
    List<Contract> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT c FROM Contract c WHERE c.site.id = :siteId")
    List<Contract> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT c FROM Contract c WHERE c.defectsEndDate <= :date AND c.status = 'COMPLETED'")
    List<Contract> findDefectsPeriodEnding(@Param("date") LocalDate date);

    boolean existsByContractRef(String contractRef);

    @Query("SELECT c FROM Contract c WHERE c.site.id = :siteId")
    Page<Contract> findBySiteId(@Param("siteId") Long siteId, Pageable pageable);

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.client.id = :clientId")
    Page<Contract> findByClientId(@Param("clientId") Long clientId, Pageable pageable);
}
