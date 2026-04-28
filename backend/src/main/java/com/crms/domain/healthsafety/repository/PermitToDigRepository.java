package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.PermitToDig;
import com.crms.domain.healthsafety.enums.PermitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermitToDigRepository extends JpaRepository<PermitToDig, Long> {

    Optional<PermitToDig> findByPermitNumber(String permitNumber);

    List<PermitToDig> findBySiteId(Long siteId);

    @Query("SELECT p FROM PermitToDig p WHERE p.site.id = :siteId AND p.status = :status")
    List<PermitToDig> findBySiteIdAndStatus(@Param("siteId") Long siteId, @Param("status") PermitStatus status);

    @Query("SELECT p FROM PermitToDig p WHERE p.status IN ('ISSUED', 'IN_PROGRESS') AND p.endDate < :date")
    List<PermitToDig> findExpiredPermits(@Param("date") LocalDate date);

    @Query("SELECT p FROM PermitToDig p WHERE p.site.id = :siteId AND p.status = 'IN_PROGRESS'")
    Optional<PermitToDig> findActivePermitForSite(@Param("siteId") Long siteId);
}
