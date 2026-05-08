package com.crms.domain.material.repository;

import com.crms.domain.material.entity.PurchaseRequisition;
import com.crms.domain.material.enums.PurchaseRequisitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {

    Optional<PurchaseRequisition> findByRequisitionRef(String requisitionRef);

    List<PurchaseRequisition> findByStatus(PurchaseRequisitionStatus status);

    @Query("SELECT p FROM PurchaseRequisition p WHERE p.requestedBy.id = :userId AND p.status = :status")
    List<PurchaseRequisition> findByRequestedByAndStatus(@Param("userId") Long userId, @Param("status") PurchaseRequisitionStatus status);

    @Query("SELECT p FROM PurchaseRequisition p LEFT JOIN FETCH p.lines WHERE p.id = :id")
    Optional<PurchaseRequisition> findByIdWithLines(@Param("id") Long id);

    boolean existsByRequisitionRef(String requisitionRef);

    Page<PurchaseRequisition> findByStatus(PurchaseRequisitionStatus status, Pageable pageable);

    Page<PurchaseRequisition> findBySite_Id(Long siteId, Pageable pageable);

    Page<PurchaseRequisition> findByStatusAndSite_Id(PurchaseRequisitionStatus status, Long siteId, Pageable pageable);
}
