package com.crms.service.impl;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.PayLessNotice;
import com.crms.domain.contract.entity.PaymentNotice;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.NoticeType;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.PaymentNoticeRepository;
import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.request.PayLessNoticeRequest;
import com.crms.dto.request.PaymentNoticeRequest;
import com.crms.dto.response.ApplicationResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.ApplicationForPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationForPaymentServiceImpl implements ApplicationForPaymentService {
    
    private final ApplicationForPaymentRepository applicationRepository;
    private final ContractRepository contractRepository;
    private final PaymentNoticeRepository paymentNoticeRepository;
    
    @Override
    public PageResponse<ApplicationResponse> findByContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        
        List<ApplicationResponse> content = applicationRepository.findByContractIdOrderByApplicationPeriodEndDesc(contractId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<ApplicationResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }
    
    @Override
    public ApplicationResponse findById(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));
        return mapToResponse(application);
    }
    
    @Override
    @Transactional
    public ApplicationResponse create(Long contractId, ApplicationForPaymentRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        
        // Calculate next application number
        Integer maxAppNumber = applicationRepository.findMaxApplicationNumberByContractId(contractId)
                .orElse(0);
        
        String applicationRef = contract.getContractRef() + "-APP-" + (maxAppNumber + 1);
        
        BigDecimal retention = request.getRetention() != null ? 
                request.getRetention() : 
                contract.calculateRetention(request.getValueOfWorks());
        
        ApplicationForPayment application = ApplicationForPayment.builder()
                .contract(contract)
                .applicationRef(applicationRef)
                .applicationNumber(maxAppNumber + 1)
                .applicationPeriodStart(request.getApplicationPeriodStart())
                .applicationPeriodEnd(request.getApplicationPeriodEnd())
                .valueOfWorks(request.getValueOfWorks())
                .retention(retention)
                .grossValue(request.getValueOfWorks().subtract(retention))
                .status(ApplicationStatus.DRAFT)
                .build();
        
        // Calculate due date based on contract terms
        LocalDate dueDate = request.getApplicationPeriodEnd()
                .plusDays(contract.getPaymentTermsDays() != null ? contract.getPaymentTermsDays() : 30);
        application.setDueDate(dueDate);
        
        application = applicationRepository.save(application);
        return mapToResponse(application);
    }
    
    @Override
    @Transactional
    public ApplicationResponse submit(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));
        
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ValidationException("Application can only be submitted from DRAFT status");
        }
        
        Contract contract = application.getContract();
        
        // Recalculate dates based on contract terms
        LocalDate dueDate = application.getApplicationPeriodEnd()
                .plusDays(contract.getPaymentTermsDays() != null ? contract.getPaymentTermsDays() : 30);
        application.setDueDate(dueDate);
        
        // Calculate final date for payment
        LocalDate finalDateForPayment = dueDate.plusDays(
                contract.getFinalDateForPaymentOffsetDays() != null ? contract.getFinalDateForPaymentOffsetDays() : 14);
        
        application.setSubmittedDate(LocalDate.now());
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setPayerRef("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Create default payment notice
        PaymentNotice defaultNotice = PaymentNotice.builder()
                .application(application)
                .noticeType(NoticeType.PAYMENT)
                .issuedOn(LocalDateTime.now())
                .sumConsideredDue(application.getGrossValue())
                .currency("GBP")
                .basisOfCalculation("Application submitted for period " + 
                        application.getApplicationPeriodStart() + " to " + application.getApplicationPeriodEnd())
                .documentRef("NOTICE-" + application.getApplicationRef())
                .finalDateForPayment(finalDateForPayment.atStartOfDay())
                .deadlineForPayLessNotice(finalDateForPayment.minusDays(
                        contract.getPayLessNoticePrescribedPeriodDays() != null ? 
                        contract.getPayLessNoticePrescribedPeriodDays() : 7).atStartOfDay())
                .build();
        
        // Calculate SHA-256 hash of notice content
        String noticeContent = buildNoticeContent(application);
        defaultNotice.setSha256(calculateSHA256(noticeContent));
        
        // Generate audit log ID
        defaultNotice.setAuditLogId(UUID.randomUUID().toString());
        
        paymentNoticeRepository.save(defaultNotice);
        
        application = applicationRepository.save(application);
        log.info("Application {} submitted, payment notice {} created", 
                application.getApplicationRef(), defaultNotice.getDocumentRef());
        
        return mapToResponse(application);
    }
    
    @Override
    @Transactional
    public ApplicationResponse addPaymentNotice(Long id, PaymentNoticeRequest request) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));
        
        Contract contract = application.getContract();
        LocalDate finalDateForPayment = application.getDueDate() != null ? 
                application.getDueDate().plusDays(contract.getFinalDateForPaymentOffsetDays() != null ? 
                        contract.getFinalDateForPaymentOffsetDays() : 14) : LocalDate.now();
        
        PaymentNotice paymentNotice = PaymentNotice.builder()
                .application(application)
                .noticeType(request.getNoticeType())
                .issuedOn(request.getIssuedOn())
                .sumConsideredDue(request.getSumConsideredDue())
                .currency(request.getCurrency() != null ? request.getCurrency() : "GBP")
                .basisOfCalculation(request.getBasisOfCalculation())
                .documentRef("NOTICE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .finalDateForPayment(finalDateForPayment.atStartOfDay())
                .deadlineForPayLessNotice(finalDateForPayment.minusDays(
                        contract.getPayLessNoticePrescribedPeriodDays() != null ? 
                        contract.getPayLessNoticePrescribedPeriodDays() : 7).atStartOfDay())
                .build();
        
        String noticeContent = buildNoticeContent(application) + request.getBasisOfCalculation();
        paymentNotice.setSha256(calculateSHA256(noticeContent));
        paymentNotice.setAuditLogId(UUID.randomUUID().toString());
        
        paymentNoticeRepository.save(paymentNotice);
        
        log.info("Payment notice added to application {}", application.getApplicationRef());
        return mapToResponse(application);
    }
    
    @Override
    @Transactional
    public ApplicationResponse addPayLessNotice(Long id, PayLessNoticeRequest request) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));
        
        if (application.getPayLessNotice() != null) {
            throw new ValidationException("Pay less notice already exists for this application");
        }
        
        Contract contract = application.getContract();
        LocalDate finalDateForPayment = application.getDueDate() != null ? 
                application.getDueDate().plusDays(contract.getFinalDateForPaymentOffsetDays() != null ? 
                        contract.getFinalDateForPaymentOffsetDays() : 14) : LocalDate.now();
        
        PayLessNotice payLessNotice = PayLessNotice.builder()
                .application(application)
                .issuedOn(request.getIssuedOn())
                .sumConsideredDue(request.getSumConsideredDue())
                .currency(request.getCurrency() != null ? request.getCurrency() : "GBP")
                .basisOfCalculation(request.getBasisOfCalculation())
                .documentRef("PLN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .finalDateForPayment(finalDateForPayment.atStartOfDay())
                .deadlineForPayLessNotice(finalDateForPayment.minusDays(
                        contract.getPayLessNoticePrescribedPeriodDays() != null ? 
                        contract.getPayLessNoticePrescribedPeriodDays() : 7).atStartOfDay())
                .build();
        
        String noticeContent = buildNoticeContent(application) + request.getBasisOfCalculation();
        payLessNotice.setSha256(calculateSHA256(noticeContent));
        payLessNotice.setAuditLogId(UUID.randomUUID().toString());
        
        application.setPayLessNotice(payLessNotice);
        application = applicationRepository.save(application);
        
        log.info("Pay less notice added to application {}", application.getApplicationRef());
        return mapToResponse(application);
    }
    
    @Override
    @Transactional
    public ApplicationResponse addDefaultNotice(Long id) {
        return submit(id);
    }
    
    private ApplicationResponse mapToResponse(ApplicationForPayment application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .applicationRef(application.getApplicationRef())
                .applicationNumber(application.getApplicationNumber())
                .contractId(application.getContract().getId())
                .contractRef(application.getContract().getContractRef())
                .applicationPeriodStart(application.getApplicationPeriodStart())
                .applicationPeriodEnd(application.getApplicationPeriodEnd())
                .dueDate(application.getDueDate())
                .valueOfWorks(application.getValueOfWorks())
                .retention(application.getRetention())
                .grossValue(application.getGrossValue())
                .status(application.getStatus() != null ? application.getStatus().name() : null)
                .submittedDate(application.getSubmittedDate())
                .payerRef(application.getPayerRef())
                .build();
    }
    
    private String buildNoticeContent(ApplicationForPayment application) {
        return String.format("%s|%s|%s|%s|%s|%s",
                application.getApplicationRef(),
                application.getApplicationPeriodStart(),
                application.getApplicationPeriodEnd(),
                application.getGrossValue(),
                application.getContract().getContractRef(),
                LocalDate.now());
    }
    
    private String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return null;
        }
    }
}