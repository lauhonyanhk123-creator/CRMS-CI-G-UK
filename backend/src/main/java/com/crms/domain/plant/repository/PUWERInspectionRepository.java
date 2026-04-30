package com.crms.domain.plant.repository;

import com.crms.domain.plant.entity.PUWERInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PUWERInspectionRepository extends JpaRepository<PUWERInspection, Long> {

    List<PUWERInspection> findByPlantId(Long plantId);

    @Query("SELECT p FROM PUWERInspection p WHERE p.nextDueDate <= :date")
    List<PUWERInspection> findDueInspections(@Param("date") LocalDate date);

    @Query("SELECT p FROM PUWERInspection p WHERE p.plant.id = :plantId ORDER BY p.inspectionDate DESC")
    List<PUWERInspection> findByPlantIdOrderByInspectionDateDesc(@Param("plantId") Long plantId);
}
