package com.crms.domain.material.repository;

import com.crms.domain.material.entity.ConcreteTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConcreteTicketRepository extends JpaRepository<ConcreteTicket, Long> {

    Optional<ConcreteTicket> findByTicketNumber(String ticketNumber);

    List<ConcreteTicket> findByDeliveryNoteId(Long deliveryNoteId);

    @Query("SELECT c FROM ConcreteTicket c WHERE c.deliveryNote.site.id = :siteId AND c.deliveryNote.deliveryDate = :date")
    List<ConcreteTicket> findBySiteAndDate(@Param("siteId") Long siteId, @Param("date") LocalDate date);
}
