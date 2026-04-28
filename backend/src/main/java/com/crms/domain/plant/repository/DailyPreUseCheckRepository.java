package com.crms.domain.plant.repository;

import com.crms.domain.plant.entity.DailyPreUseCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPreUseCheckRepository extends JpaRepository<DailyPreUseCheck, Long> {

    List<DailyPreUseCheck> findByCheckDate(LocalDate checkDate);

    List<DailyPreUseCheck> findByPlantId(Long plantId);

    @Query("SELECT d FROM DailyPreUseCheck d WHERE d.plant.id = :plantId AND d.checkDate BETWEEN :start AND :end")
    List<DailyPreUseCheck> findByPlantAndDateRange(@Param("plantId") Long plantId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT d FROM DailyPreUseCheck d WHERE d.site.id = :siteId AND d.checkDate = :date")
    List<DailyPreUseCheck> findBySiteAndDate(@Param("siteId") Long siteId, @Param("date") LocalDate date);
}
