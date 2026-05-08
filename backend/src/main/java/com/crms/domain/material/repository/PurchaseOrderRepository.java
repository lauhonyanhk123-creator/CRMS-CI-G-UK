package com.crms.domain.material.repository;

import com.crms.domain.material.entity.PurchaseOrder;
import com.crms.domain.material.enums.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPurchaseOrderRef(String purchaseOrderRef);

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    @Query("SELECT p FROM PurchaseOrder p WHERE p.supplier.id = :supplierId AND p.status = :status")
    List<PurchaseOrder> findBySupplierAndStatus(@Param("supplierId") Long supplierId, @Param("status") PurchaseOrderStatus status);

    @Query("SELECT p FROM PurchaseOrder p LEFT JOIN FETCH p.lines WHERE p.id = :id")
    Optional<PurchaseOrder> findByIdWithLines(@Param("id") Long id);

    @Query("SELECT SUM(p.totalValue) FROM PurchaseOrder p WHERE p.supplier.id = :supplierId AND p.status = 'RECEIVED'")
    Optional<BigDecimal> sumReceivedValueBySupplierId(@Param("supplierId") Long supplierId);

    boolean existsByPurchaseOrderRef(String purchaseOrderRef);

    @Query("""
            SELECT COALESCE(SUM(p.netValue), 0)
            FROM PurchaseOrder p
            WHERE p.site.id = :siteId
              AND p.status IN ('RECEIVED', 'PARTIALLY_RECEIVED')
              AND p.deliveryDate >= :from
              AND p.deliveryDate <= :to
            """)
    BigDecimal sumReceivedNetValueBySiteAndDateRange(
            @Param("siteId") Long siteId,
            @Param("from") java.time.LocalDate from,
            @Param("to") java.time.LocalDate to);
}
