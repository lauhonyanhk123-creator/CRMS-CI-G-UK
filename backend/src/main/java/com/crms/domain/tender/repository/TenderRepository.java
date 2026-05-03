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

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.status = :status")
    List<Tender> findByStatus(@Param("status") TenderStatus status);
    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.status = :status")
    Page<Tender> findByStatus(@Param("status") TenderStatus status, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Tender> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.client.id = :clientId")
    List<Tender> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.client.id = :clientId")
    Page<Tender> findByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.site.id = :siteId")
    List<Tender> findBySiteId(@Param("siteId") Long siteId);

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.site.id = :siteId")
    Page<Tender> findBySiteId(@Param("siteId") Long siteId, Pageable pageable);
    
    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site")
    Page<Tender> findAll(Pageable pageable);

    @Query("SELECT DISTINCT t FROM Tender t LEFT JOIN FETCH t.client LEFT JOIN FETCH t.site WHERE t.status IN :statuses")
    List<Tender> findByStatusIn(@Param("statuses") List<TenderStatus> statuses);
    boolean existsByTenderRef(String tenderRef);
}
