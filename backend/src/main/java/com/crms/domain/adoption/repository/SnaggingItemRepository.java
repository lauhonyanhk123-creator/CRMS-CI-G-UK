package com.crms.domain.adoption.repository;

import com.crms.domain.adoption.entity.SnaggingItem;
import com.crms.domain.adoption.enums.SnaggingItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SnaggingItemRepository extends JpaRepository<SnaggingItem, Long> {

    List<SnaggingItem> findByAdoptionCaseId(Long adoptionCaseId);

    Page<SnaggingItem> findByAdoptionCaseId(Long adoptionCaseId, Pageable pageable);

    List<SnaggingItem> findByStatus(SnaggingItemStatus status);

    @Query("SELECT s FROM SnaggingItem s WHERE s.adoptionCase.id = :caseId AND s.status = :status")
    List<SnaggingItem> findByAdoptionCaseIdAndStatus(
            @Param("caseId") Long caseId, 
            @Param("status") SnaggingItemStatus status);

    @Query("SELECT s FROM SnaggingItem s WHERE s.targetCompletionDate <= :date AND s.status NOT IN (:completedStatuses)")
    List<SnaggingItem> findOverdueItems(
            @Param("date") LocalDate date,
            @Param("completedStatuses") List<SnaggingItemStatus> completedStatuses);

    @Query("SELECT s FROM SnaggingItem s WHERE s.adoptionCase.contract.id = :contractId")
    List<SnaggingItem> findByContractId(@Param("contractId") Long contractId);

    @Query("SELECT COUNT(s) FROM SnaggingItem s WHERE s.adoptionCase.id = :caseId AND s.status = :status")
    long countByAdoptionCaseIdAndStatus(
            @Param("caseId") Long caseId, 
            @Param("status") SnaggingItemStatus status);

    @Query("SELECT s FROM SnaggingItem s WHERE s.priority = :priority AND s.status NOT IN (:completedStatuses)")
    List<SnaggingItem> findByPriorityAndNotCompleted(
            @Param("priority") com.crms.domain.adoption.enums.SnaggingItemPriority priority,
            @Param("completedStatuses") List<SnaggingItemStatus> completedStatuses);
}
