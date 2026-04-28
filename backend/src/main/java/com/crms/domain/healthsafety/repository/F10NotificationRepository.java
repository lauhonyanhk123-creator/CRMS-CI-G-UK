package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.F10Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface F10NotificationRepository extends JpaRepository<F10Notification, Long> {

    Optional<F10Notification> findByNotificationNumber(String notificationNumber);

    @Query("SELECT f FROM F10Notification f WHERE f.contract.id = :contractId AND f.isActive = true")
    List<F10Notification> findActiveByContractId(@Param("contractId") Long contractId);

    @Query("SELECT f FROM F10Notification f WHERE f.constructionEndDate <= :date AND f.isActive = true")
    List<F10Notification> findExpiringNotifications(@Param("date") LocalDate date);
}
