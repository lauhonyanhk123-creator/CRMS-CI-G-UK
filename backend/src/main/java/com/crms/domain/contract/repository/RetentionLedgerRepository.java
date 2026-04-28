package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.RetentionLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RetentionLedgerRepository extends JpaRepository<RetentionLedger, Long> {

    Optional<RetentionLedger> findByContractId(Long contractId);
}
