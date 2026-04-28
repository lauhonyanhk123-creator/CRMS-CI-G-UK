package com.crms.domain.material.repository;

import com.crms.domain.material.entity.CubeSample;
import com.crms.domain.material.enums.CubeTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CubeSampleRepository extends JpaRepository<CubeSample, Long> {

    List<CubeSample> findByConcreteTicketId(Long concreteTicketId);

    List<CubeSample> findByResult(CubeTestResult result);

    @Query("SELECT c FROM CubeSample c WHERE c.result = 'PENDING' AND c.castDate < :date")
    List<CubeSample> findPendingTestsOlderThan(@Param("date") LocalDate date);

    @Query("SELECT c FROM CubeSample c WHERE c.concreteTicket.deliveryNote.site.id = :siteId")
    List<CubeSample> findBySiteId(@Param("siteId") Long siteId);
}
