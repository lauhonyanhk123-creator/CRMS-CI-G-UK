package com.crms.domain.tender.repository;

import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {

    Optional<Tender> findByTenderRef(String tenderRef);

    List<Tender> findByStatus(TenderStatus status);

    Page<Tender> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT t FROM Tender t WHERE t.client.id = :clientId")
    List<Tender> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT t FROM Tender t WHERE t.client.id = :clientId")
    Page<Tender> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query("SELECT t FROM Tender t WHERE t.site.id = :siteId")
    List<Tender> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT t FROM Tender t WHERE t.site.id = :siteId")
    Page<Tender> findBySiteId(@Param("siteId") Long siteId, Pageable pageable);

    boolean existsByTenderRef(String tenderRef);
}
