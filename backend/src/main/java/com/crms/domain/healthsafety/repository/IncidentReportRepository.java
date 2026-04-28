package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.IncidentReport;
import com.crms.domain.healthsafety.enums.IncidentStatus;
import com.crms.domain.healthsafety.enums.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    Optional<IncidentReport> findByReportNumber(String reportNumber);

    List<IncidentReport> findBySiteId(Long siteId);

    List<IncidentReport> findByStatus(IncidentStatus status);

    @Query("SELECT i FROM IncidentReport i WHERE i.site.id = :siteId AND i.status = :status")
    List<IncidentReport> findBySiteIdAndStatus(@Param("siteId") Long siteId, @Param("status") IncidentStatus status);

    @Query("SELECT i FROM IncidentReport i WHERE i.ridDORNotifiable = true AND i.status != 'CLOSED'")
    List<IncidentReport> findRIDDORNotifiableOpen();

    @Query("SELECT i FROM IncidentReport i WHERE i.incidentDate BETWEEN :start AND :end")
    List<IncidentReport> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
