package com.crms.domain.subcontractor.repository;

import com.crms.domain.subcontractor.entity.CISVerification;
import com.crms.domain.subcontractor.enums.CisVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CISVerificationRepository extends JpaRepository<CISVerification, Long> {

    Optional<CISVerification> findByVerificationRef(String verificationRef);

    List<CISVerification> findByCompanyId(Long companyId);
    List<CISVerification> findByStatus(CisVerificationStatus status);

    @Query("SELECT c FROM CISVerification c WHERE c.company.id = :companyId AND c.status = :status")
    List<CISVerification> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") CisVerificationStatus status);

    @Query("SELECT c FROM CISVerification c WHERE c.expiresAt <= :date AND c.status = 'VERIFIED'")
    List<CISVerification> findExpiringVerifications(@Param("date") LocalDate date);

    @Query("SELECT c FROM CISVerification c WHERE c.company.id = :companyId AND c.status = 'VERIFIED' AND c.expiresAt > :date")
    Optional<CISVerification> findValidVerification(@Param("companyId") Long companyId, @Param("date") LocalDate date);
}
