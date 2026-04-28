package com.crms.domain.material.repository;

import com.crms.domain.material.entity.SupplierPriceListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierPriceListItemRepository extends JpaRepository<SupplierPriceListItem, Long> {

    List<SupplierPriceListItem> findByMaterialId(Long materialId);

    List<SupplierPriceListItem> findBySupplierId(Long supplierId);

    @Query("SELECT s FROM SupplierPriceListItem s WHERE s.material.id = :materialId AND s.supplier.id = :supplierId AND (s.validFrom IS NULL OR s.validFrom <= :date) AND (s.validTo IS NULL OR s.validTo >= :date)")
    Optional<SupplierPriceListItem> findValidPrice(@Param("materialId") Long materialId, @Param("supplierId") Long supplierId, @Param("date") LocalDate date);
}
