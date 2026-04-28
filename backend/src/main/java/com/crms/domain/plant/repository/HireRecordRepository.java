package com.crms.domain.plant.repository;

import com.crms.domain.plant.entity.HireRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HireRecordRepository extends JpaRepository<HireRecord, Long> {

    List<HireRecord> findByPlantId(Long plantId);

    @Query("SELECT h FROM HireRecord h WHERE h.plant.id = :plantId AND h.offHireDate IS NULL")
    Optional<HireRecord> findActiveHireByPlantId(@Param("plantId") Long plantId);

    @Query("SELECT SUM(h.dailyRate * :days) FROM HireRecord h WHERE h.plant.id = :plantId AND h.offHireDate IS NULL")
    Optional<BigDecimal> calculateCurrentHireCost(@Param("plantId") Long plantId, @Param("days") long days);
}
