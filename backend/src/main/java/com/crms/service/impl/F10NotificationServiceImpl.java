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
import com.crms.util.PaginationHelper;
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
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

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
                .moreThan30Days(request.getMoreThan30Days())
                .moreThan500PersonDays(request.getMoreThan500PersonDays())
                .constructionStartDate(request.getConstructionStartDate())
                .constructionEndDate(request.getConstructionEndDate())
                .isActive(true)
                .hdfAcknowledged(false)
                .build();

        notification = f10NotificationRepository.save(notification);

        log.info("Created F10 notification {} for contract {}", notificationNumber, contract.getContractRef());
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse update(Long id, F10NotificationRequest request) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));

        notification.setMoreThan30Days(request.getMoreThan30Days());
        notification.setMoreThan500PersonDays(request.getMoreThan500PersonDays());
        notification.setConstructionStartDate(request.getConstructionStartDate());
        notification.setConstructionEndDate(request.getConstructionEndDate());

        notification = f10NotificationRepository.save(notification);

        log.info("Updated F10 notification {}", notification.getNotificationNumber());
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse submitToHSE(Long id) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));

        if (notification.getSubmittedDate() != null) {
            throw new IllegalStateException("Notification has already been submitted");
        }

        notification.setSubmittedDate(LocalDate.now());

        // Generate confirmation number (simulated)
        notification.setConfirmationNumber("HSE-" + System.currentTimeMillis());

        // If HDF is required, mark that it needs to be submitted
        if (notification.requiresHDF()) {
            notification.setHdfSubmittedDate(LocalDate.now());
        }

        notification = f10NotificationRepository.save(notification);

        log.info("Submitted F10 notification {} to HSE", notification.getNotificationNumber());
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public F10NotificationResponse acknowledgeHDF(Long id) {
        F10Notification notification = f10NotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification", id));

        if (!notification.requiresHDF()) {
            throw new IllegalStateException("This notification does not require HDF acknowledgment");
        }

        notification.setHdfAcknowledged(true);
        notification.setHdfAcknowledgedDate(LocalDateTime.now());
        notification.setHdfAcknowledgedBy("SYSTEM"); // In real app, get from security context

        notification = f10NotificationRepository.save(notification);

        log.info("Acknowledged HDF for F10 notification {}", notification.getNotificationNumber());
        return mapToResponse(notification);
    }

    @Override
    public F10NotificationResponse findActiveByContractId(Long contractId) {
        List<F10Notification> notifications = f10NotificationRepository.findActiveByContractId(contractId);

        if (notifications.isEmpty()) {
            throw new ResourceNotFoundException("No active F10 notification found for contract: " + contractId);
        }

        // Return the first active notification
        return mapToResponse(notifications.get(0));
    }

    @Override
    public F10NotificationResponse findByNotificationNumber(String notificationNumber) {
        F10Notification notification = f10NotificationRepository.findByNotificationNumber(notificationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("F10Notification with number " + notificationNumber));
        return mapToResponse(notification);
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
