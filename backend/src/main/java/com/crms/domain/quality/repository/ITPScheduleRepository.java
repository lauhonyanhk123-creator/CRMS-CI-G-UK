package com.crms.domain.quality.repository;

import com.crms.domain.quality.entity.ITPSchedule;
import com.crms.domain.quality.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ITPScheduleRepository extends JpaRepository<ITPSchedule, Long> {

    Page<ITPSchedule> findByContractId(Long contractId, Pageable pageable);

    Page<ITPSchedule> findByStatus(ScheduleStatus status, Pageable pageable);

    @Query("SELECT s FROM ITPSchedule s WHERE " +
           "(:contractId IS NULL OR s.contract.id = :contractId) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<ITPSchedule> findByFilters(
        @Param("contractId") Long contractId,
        @Param("status") ScheduleStatus status,
        Pageable pageable
    );

    @Query("SELECT s FROM ITPSchedule s WHERE s.dueDate <= :date AND s.status = :status")
    List<ITPSchedule> findByDueDateBeforeAndStatus(
        @Param("date") LocalDate date,
        @Param("status") ScheduleStatus status
    );

    List<ITPSchedule> findByAssignedInspector(String inspector);
}
