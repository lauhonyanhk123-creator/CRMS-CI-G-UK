package com.crms.service;

import com.crms.dto.request.CDMRegisterRequest;
import com.crms.dto.response.CDMRegisterResponse;
import com.crms.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.Map;

public interface CDMRegisterService {

    PageResponse<CDMRegisterResponse> findAll(Map<String, Object> params);

    CDMRegisterResponse findById(Long id);

    CDMRegisterResponse create(CDMRegisterRequest request);

    CDMRegisterResponse update(Long id, CDMRegisterRequest request);

    CDMRegisterResponse submitToHSE(Long id);

    CDMRegisterResponse createHealthSafetyFile(Long id);

    CDMRegisterResponse completeHealthSafetyFile(Long id);

    CDMRegisterResponse findByNotificationNumber(String notificationNumber);

    PageResponse<CDMRegisterResponse> findActiveByClientId(Long clientId);

    PageResponse<CDMRegisterResponse> findExpiringProjects(LocalDate date);

    PageResponse<CDMRegisterResponse> findPendingHseNotification();
}
