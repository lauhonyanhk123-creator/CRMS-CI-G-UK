package com.crms.domain.material.repository;

import java.util.Optional;
import com.crms.domain.material.entity.MuckawayTicket;
import com.crms.domain.material.enums.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MuckawayTicketRepository extends JpaRepository<MuckawayTicket, Long> {

    Optional<MuckawayTicket> findByTicketNumber(String ticketNumber);

    List<MuckawayTicket> findByDeliveryNoteId(Long deliveryNoteId);

    List<MuckawayTicket> findByWasteType(WasteType wasteType);

    @Query("SELECT m FROM MuckawayTicket m WHERE m.deliveryNote.site.id = :siteId AND m.deliveryNote.deliveryDate BETWEEN :start AND :end")
    List<MuckawayTicket> findBySiteAndDateRange(@Param("siteId") Long siteId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(m.disposalCost) FROM MuckawayTicket m WHERE m.deliveryNote.site.id = :siteId AND m.deliveryNote.deliveryDate BETWEEN :start AND :end")
    Optional<BigDecimal> sumDisposalCostBySiteAndDateRange(@Param("siteId") Long siteId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
