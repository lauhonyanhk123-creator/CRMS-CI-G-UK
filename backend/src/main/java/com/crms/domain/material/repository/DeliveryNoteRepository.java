package com.crms.domain.material.repository;

import com.crms.domain.material.entity.DeliveryNote;
import com.crms.domain.material.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long> {

    Optional<DeliveryNote> findByDeliveryNoteRef(String deliveryNoteRef);

    List<DeliveryNote> findByStatus(DeliveryStatus status);

    List<DeliveryNote> findByOrderId(Long orderId);

    @Query("SELECT d FROM DeliveryNote d WHERE d.site.id = :siteId AND d.deliveryDate = :date")
    List<DeliveryNote> findBySiteAndDate(@Param("siteId") Long siteId, @Param("date") LocalDate date);

    @Query("SELECT d FROM DeliveryNote d WHERE d.status = 'EXPECTED' AND d.deliveryDate <= :date")
    List<DeliveryNote> findExpectedDeliveries(@Param("date") LocalDate date);
}
