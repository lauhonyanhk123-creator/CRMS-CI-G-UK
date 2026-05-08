package com.crms.domain.plant.repository;

import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlantAllocationRepository extends JpaRepository<PlantAllocation, Long> {

    List<PlantAllocation> findByPlantId(Long plantId);

    List<PlantAllocation> findBySiteId(Long siteId);

    List<PlantAllocation> findByStatus(AllocationStatus status);

    @Query("SELECT p FROM PlantAllocation p WHERE p.site.id = :siteId AND p.status = 'ACTIVE' AND p.startDate <= :date AND (p.endDate IS NULL OR p.endDate >= :date)")
    List<PlantAllocation> findActiveBySiteAndDate(@Param("siteId") Long siteId, @Param("date") LocalDate date);

    @Query("SELECT p FROM PlantAllocation p WHERE p.plant.id = :plantId AND p.status = 'ACTIVE'")
    List<PlantAllocation> findActiveByPlantId(@Param("plantId") Long plantId);

    @Query("SELECT p FROM PlantAllocation p WHERE p.plant.id = :plantId " +
           "AND p.startDate <= :toDate " +
           "AND (p.endDate IS NULL OR p.endDate >= :fromDate)")
    List<PlantAllocation> findByPlantIdAndDateRange(
            @Param("plantId") Long plantId,
            @Param("fromDate") LocalDate from,
            @Param("toDate") LocalDate to);

    @Query("SELECT p FROM PlantAllocation p LEFT JOIN FETCH p.plant " +
           "WHERE p.site.id = :siteId " +
           "AND p.startDate <= :toDate " +
           "AND (p.endDate IS NULL OR p.endDate >= :fromDate)")
    List<PlantAllocation> findBySiteAndDateRangeWithPlant(
            @Param("siteId") Long siteId,
            @Param("fromDate") LocalDate from,
            @Param("toDate") LocalDate to);
}
