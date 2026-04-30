package com.crms.domain.plant.repository;

import com.crms.domain.plant.entity.LOLERExamination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LOLERExaminationRepository extends JpaRepository<LOLERExamination, Long> {

    List<LOLERExamination> findByPlantId(Long plantId);

    @Query("SELECT l FROM LOLERExamination l WHERE l.plant.id = :plantId ORDER BY l.examinationDate DESC")
    List<LOLERExamination> findByPlantIdOrderByExaminationDateDesc(@Param("plantId") Long plantId);

    @Query("SELECT l FROM LOLERExamination l WHERE l.nextDueDate <= :date")
    List<LOLERExamination> findDueExaminations(@Param("date") LocalDate date);

    @Query("SELECT l FROM LOLERExamination l WHERE l.plant.id = :plantId ORDER BY l.nextDueDate DESC")
    Optional<LOLERExamination> findLatestByPlantId(@Param("plantId") Long plantId);
}
