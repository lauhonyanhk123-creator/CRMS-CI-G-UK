package com.crms.domain.quality.repository;

import com.crms.domain.quality.entity.InspectionRecord;
import com.crms.domain.quality.enums.InspectionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, Long> {

    Page<InspectionRecord> findByScheduleItemId(Long scheduleItemId, Pageable pageable);

    Page<InspectionRecord> findByResult(InspectionResult result, Pageable pageable);

    @Query("SELECT i FROM InspectionRecord i WHERE " +
           "(:scheduleItemId IS NULL OR i.scheduleItem.id = :scheduleItemId) AND " +
           "(:result IS NULL OR i.result = :result) AND " +
           "(:inspectorName IS NULL OR i.inspectorName LIKE %:inspectorName%)")
    Page<InspectionRecord> findByFilters(
        @Param("scheduleItemId") Long scheduleItemId,
        @Param("result") InspectionResult result,
        @Param("inspectorName") String inspectorName,
        Pageable pageable
    );

    List<InspectionRecord> findByInspectionDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT i FROM InspectionRecord i WHERE i.scheduleItem.schedule.contract.id = :contractId")
    Page<InspectionRecord> findByContractId(@Param("contractId") Long contractId, Pageable pageable);
}
