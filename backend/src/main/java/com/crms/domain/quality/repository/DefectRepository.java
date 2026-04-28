package com.crms.domain.quality.repository;

import com.crms.domain.quality.entity.Defect;
import com.crms.domain.quality.enums.DefectPriority;
import com.crms.domain.quality.enums.DefectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {

    Page<Defect> findByContractId(Long contractId, Pageable pageable);

    Page<Defect> findByStatus(DefectStatus status, Pageable pageable);

    Page<Defect> findByPriority(DefectPriority priority, Pageable pageable);

    @Query("SELECT d FROM Defect d WHERE " +
           "(:contractId IS NULL OR d.contract.id = :contractId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:priority IS NULL OR d.priority = :priority)")
    Page<Defect> findByFilters(
        @Param("contractId") Long contractId,
        @Param("status") DefectStatus status,
        @Param("priority") DefectPriority priority,
        Pageable pageable
    );

    List<Defect> findByAssignedContractor(String contractor);

    List<Defect> findByDueDateBeforeAndStatus(LocalDate date, DefectStatus status);

    @Query("SELECT d FROM Defect d WHERE d.reinspectionRequired = true AND d.reinspectionDate <= :date")
    List<Defect> findOverdueReinspections(@Param("date") LocalDate date);

    @Query("SELECT COUNT(d) FROM Defect d WHERE d.contract.id = :contractId AND d.status = :status")
    Long countByContractAndStatus(@Param("contractId") Long contractId, @Param("status") DefectStatus status);
}
