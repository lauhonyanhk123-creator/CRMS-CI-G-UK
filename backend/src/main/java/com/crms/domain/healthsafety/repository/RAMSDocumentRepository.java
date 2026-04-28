package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.RAMSDocument;
import com.crms.domain.healthsafety.enums.RamsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RAMSDocumentRepository extends JpaRepository<RAMSDocument, Long> {

    List<RAMSDocument> findByContractId(Long contractId);

    @Query("SELECT r FROM RAMSDocument r WHERE r.contract.id = :contractId AND r.status = :status")
    List<RAMSDocument> findByContractIdAndStatus(@Param("contractId") Long contractId, @Param("status") RamsStatus status);

    @Query("SELECT r FROM RAMSDocument r WHERE r.validUntil <= :date AND r.status = 'SIGNED'")
    List<RAMSDocument> findExpiringDocuments(@Param("date") LocalDate date);

    Optional<RAMSDocument> findTopByContractIdOrderByVersionDesc(Long contractId);
}
