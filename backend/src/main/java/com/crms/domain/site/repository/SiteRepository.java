package com.crms.domain.site.repository;

import com.crms.domain.company.entity.Company;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.enums.SiteStatus;
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
public interface SiteRepository extends JpaRepository<Site, Long> {

    Optional<Site> findBySiteCode(String siteCode);

    List<Site> findByClient(Company client);

    List<Site> findByStatus(SiteStatus status);
    org.springframework.data.domain.Page<Site> findByClientId(Long clientId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Site> findByStatus(SiteStatus status, org.springframework.data.domain.Pageable pageable);

    Page<Site> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT s FROM Site s WHERE s.status = :status")
    List<Site> findActiveSites(@Param("status") SiteStatus status);

    @Query("SELECT s FROM Site s WHERE s.completionDate <= :date AND s.status = 'ACTIVE'")
    List<Site> findSitesNearingCompletion(@Param("date") LocalDate date);

    boolean existsBySiteCode(String siteCode);
    long countByStatus(SiteStatus status);
}
