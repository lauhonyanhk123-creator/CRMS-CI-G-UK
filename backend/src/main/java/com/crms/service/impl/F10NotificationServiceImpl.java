package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.healthsafety.entity.F10Notification;
import com.crms.domain.healthsafety.repository.F10NotificationRepository;
import com.crms.dto.request.F10NotificationRequest;
import com.crms.dto.response.F10NotificationResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.F10NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class F10NotificationServiceImpl implements F10NotificationService {

    private final F10NotificationRepository f10NotificationRepository;
    private final ContractRepository contractRepository;

    @Override
    public PageResponse<F10NotificationResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "id";

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<F10Notification> notificationPage = f10NotificationRepository.findAll(pageable);

        List<F10NotificationResponse> content = notificationPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<F10NotificationResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .build();
    }

    @Override
    public F10NotificationResponse findById(Long id) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse create(Long contractId, F10NotificationRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String notificationNumber = generateNotificationNumber();

        F10Notification notification = F10Notification.builder()
                .contract(contract)
                .notificationNumber(notificationNumber)
                .submittedDate(request.getSubmittedDate())
                .confirmationNumber(request.getConfirmationNumber())
                .moreThan30Days(request.getMoreThan30Days())
                .moreThan500PersonDays(request.getMoreThan500PersonDays())
                .constructionStartDate(request.getConstructionStartDate())
                .constructionEndDate(request.getConstructionEndDate())
                .isActive(true)
                .build();

        notification = f10NotificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse update(Long id, F10NotificationRequest request) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));

        notification.setSubmittedDate(request.getSubmittedDate());
        notification.setConfirmationNumber(request.getConfirmationNumber());
        notification.setMoreThan30Days(request.getMoreThan30Days());
        notification.setMoreThan500PersonDays(request.getMoreThan500PersonDays());
        notification.setConstructionStartDate(request.getConstructionStartDate());
        notification.setConstructionEndDate(request.getConstructionEndDate());

        if (request.getHdfReference() != null) {
            notification.setHdfReference(request.getHdfReference());
        }
        if (request.getHdfSubmittedDate() != null) {
            notification.setHdfSubmittedDate(request.getHdfSubmittedDate());
        }
        if (request.getHdfAcknowledged() != null) {
            notification.setHdfAcknowledged(request.getHdfAcknowledged());
        }
        if (request.getHdfAcknowledgedBy() != null) {
            notification.setHdfAcknowledgedBy(request.getHdfAcknowledgedBy());
        }
        if (request.getHdfAcknowledgedDate() != null) {
            notification.setHdfAcknowledgedDate(request.getHdfAcknowledgedDate());
        }

        notification = f10NotificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse submitToHSE(Long id) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));

        notification.setSubmittedDate(LocalDate.now());
        notification = f10NotificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse acknowledgeHDF(Long id) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));

        notification.setHdfAcknowledged(true);
        notification.setHdfAcknowledgedDate(LocalDateTime.now());
        notification = f10NotificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    public F10NotificationResponse findActiveByContractId(Long contractId) {
        List<F10Notification> notifications = f10NotificationRepository.findActiveByContractId(contractId);
        if (notifications.isEmpty()) {
            return null;
        }
        return mapToResponse(notifications.get(0));
    }

    @Override
    public F10NotificationResponse findByNotificationNumber(String notificationNumber) {
        return f10NotificationRepository.findByNotificationNumber(notificationNumber)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public PageResponse<F10NotificationResponse> findExpiringNotifications(LocalDate date) {
        List<F10Notification> notifications = f10NotificationRepository.findExpiringNotifications(date);
        List<F10NotificationResponse> content = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<F10NotificationResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    private String generateNotificationNumber() {
        String prefix = "F10";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private F10NotificationResponse mapToResponse(F10Notification notification) {
        return F10NotificationResponse.builder()
                .id(notification.getId())
                .contractId(notification.getContract() != null ? notification.getContract().getId() : null)
                .contractRef(notification.getContract() != null ? notification.getContract().getContractRef() : null)
                .notificationNumber(notification.getNotificationNumber())
                .submittedDate(notification.getSubmittedDate())
                .confirmationNumber(notification.getConfirmationNumber())
                .moreThan30Days(notification.getMoreThan30Days())
                .moreThan500PersonDays(notification.getMoreThan500PersonDays())
                .constructionStartDate(notification.getConstructionStartDate())
                .constructionEndDate(notification.getConstructionEndDate())
                .isActive(notification.getIsActive())
                .hdfReference(notification.getHdfReference())
                .hdfSubmittedDate(notification.getHdfSubmittedDate())
                .hdfAcknowledged(notification.getHdfAcknowledged())
                .hdfAcknowledgedBy(notification.getHdfAcknowledgedBy())
                .hdfAcknowledgedDate(notification.getHdfAcknowledgedDate())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
