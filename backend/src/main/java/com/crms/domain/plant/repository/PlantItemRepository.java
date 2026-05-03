package com.crms.domain.plant.repository;

import com.crms.domain.plant.entity.PlantItem;
import com.crms.domain.plant.enums.PlantCategory;
import com.crms.domain.plant.enums.PlantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlantItemRepository extends JpaRepository<PlantItem, Long> {

    Optional<PlantItem> findByPlantRef(String plantRef);

    List<PlantItem> findByStatus(PlantStatus status);

    Page<PlantItem> findByStatus(PlantStatus status, Pageable pageable);

    List<PlantItem> findByCategory(PlantCategory category);

    Page<PlantItem> findByCategory(PlantCategory category, Pageable pageable);

    Page<PlantItem> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    @Query("SELECT p FROM PlantItem p WHERE LOWER(p.plantRef) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%',:search,'%'))")
    Page<PlantItem> searchByRefOrDescription(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM PlantItem p WHERE p.status = 'AVAILABLE' AND p.category = :category")
    List<PlantItem> findAvailableByCategory(@Param("category") PlantCategory category);

    @Query("SELECT p FROM PlantItem p LEFT JOIN FETCH p.lolerExaminations WHERE p.id = :id")
    Optional<PlantItem> findByIdWithExaminations(@Param("id") Long id);

    boolean existsByPlantRef(String plantRef);
    long countByStatus(PlantStatus status);
}
