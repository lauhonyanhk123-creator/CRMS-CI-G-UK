package com.crms.domain.material.repository;

import com.crms.domain.material.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findByMaterialCode(String materialCode);

    Page<Material> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    List<Material> findByCategory(String category);

    List<Material> findByTrade(String trade);

    @Query("SELECT m FROM Material m WHERE m.supplier.id = :supplierId")
    List<Material> findBySupplierId(@Param("supplierId") Long supplierId);

    boolean existsByMaterialCode(String materialCode);
}
