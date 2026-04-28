package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.CDMRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CDMRegisterRepository extends JpaRepository<CDMRegister, Long> {

    Optional<CDMRegister> findByNotificationNumber(String notificationNumber);

    @Query("SELECT c FROM CDMRegister c WHERE c.client.id = :clientId AND c.isActive = true")
    List<CDMRegister> findActiveByClientId(@Param("clientId") Long clientId);

    @Query("SELECT c FROM CDMRegister c WHERE c.constructionEndDate <= :date AND c.isActive = true")
    List<CDMRegister> findExpiringProjects(@Param("date") LocalDate date);

    @Query("SELECT c FROM CDMRegister c WHERE c.isNotifiable = true AND c.hseNotificationRef IS NULL")
    List<CDMRegister> findPendingHseNotification();

    @Query("SELECT c FROM CDMRegister c WHERE c.site.id = :siteId AND c.isActive = true")
    List<CDMRegister> findActiveBySiteId(@Param("siteId") Long siteId);
}
