package com.crms.service;

import com.crms.dto.request.F10NotificationRequest;
import com.crms.dto.response.F10NotificationResponse;
import com.crms.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.Map;

public interface F10NotificationService {

    PageResponse<F10NotificationResponse> findAll(Map<String, Object> params);

    F10NotificationResponse findById(Long id);

    F10NotificationResponse create(Long contractId, F10NotificationRequest request);

    F10NotificationResponse update(Long id, F10NotificationRequest request);

    F10NotificationResponse submitToHSE(Long id);

    F10NotificationResponse acknowledgeHDF(Long id);

    F10NotificationResponse findActiveByContractId(Long contractId);

    F10NotificationResponse findByNotificationNumber(String notificationNumber);

    PageResponse<F10NotificationResponse> findExpiringNotifications(LocalDate date);
}
