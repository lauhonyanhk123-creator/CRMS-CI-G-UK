package com.crms.service.impl;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.PayLessNotice;
import com.crms.domain.contract.entity.PaymentNotice;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.PayLessNoticeRepository;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationForPaymentServiceImpl implements ApplicationForPaymentService {

    private final ApplicationForPaymentRepository applicationRepository;
    private final ContractRepository contractRepository;
    private final PaymentNoticeRepository paymentNoticeRepository;
    private final PayLessNoticeRepository payLessNoticeRepository;

    @Override
    public PageResponse<ApplicationResponse> findByContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        List<ApplicationForPayment> applications = applicationRepository.findByContractId(contractId);

        List<ApplicationResponse> content = applications.stream()
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

        Integer maxAppNum = applicationRepository.findMaxApplicationNumberByContractId(contractId).orElse(0);
        Integer applicationNumber = maxAppNum + 1;
        String applicationRef = contract.getContractRef() + "/AFP/" + String.format("%04d", applicationNumber);

        ApplicationForPayment application = ApplicationForPayment.builder()
                .contract(contract)
                .applicationRef(applicationRef)
                .applicationNumber(applicationNumber)
                .applicationPeriodStart(request.getApplicationPeriodStart())
                .applicationPeriodEnd(request.getApplicationPeriodEnd())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : calculateDueDate(request.getApplicationPeriodEnd()))
                .valueOfWorks(request.getValueOfWorks())
                .retention(request.getRetention())
                .status(ApplicationStatus.DRAFT)
                .build();

        application.calculateGrossValue();
        application = applicationRepository.save(application);

        log.info("Created application for payment {} for contract {}", applicationRef, contract.getContractRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse submit(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new ValidationException("Only DRAFT applications can be submitted");
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedDate(LocalDate.now());
        application = applicationRepository.save(application);

        log.info("Submitted application for payment {}", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse measure(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ValidationException("Only SUBMITTED applications can be measured");
        }

        application.setStatus(ApplicationStatus.MEASURED);
        application = applicationRepository.save(application);

        log.info("Application {} measured", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse agree(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        if (application.getStatus() != ApplicationStatus.MEASURED) {
            throw new ValidationException("Only MEASURED applications can be agreed");
        }

        application.setStatus(ApplicationStatus.AGREED);
        application = applicationRepository.save(application);

        log.info("Application {} agreed", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse approve(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        if (application.getStatus() != ApplicationStatus.AGREED) {
            throw new ValidationException("Only AGREED applications can be approved");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        application = applicationRepository.save(application);

        log.info("Application {} approved", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse reject(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        application.setStatus(ApplicationStatus.REJECTED);
        application = applicationRepository.save(application);

        log.info("Application {} rejected", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse markPaid(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new ValidationException("Only APPROVED applications can be marked as paid");
        }

        application.setStatus(ApplicationStatus.PAID);
        application.setPaidDate(LocalDate.now());
        application = applicationRepository.save(application);

        log.info("Application {} marked as paid", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse addPaymentNotice(Long id, PaymentNoticeRequest request) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        PaymentNotice paymentNotice = PaymentNotice.builder()
                .application(application)
                .noticeDate(LocalDate.now())
                .amount(request.getAmount())
                .reference(request.getReference())
                .build();

        paymentNoticeRepository.save(paymentNotice);

        log.info("Added payment notice to application {}", application.getApplicationRef());
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

        PayLessNotice payLessNotice = PayLessNotice.builder()
                .application(application)
                .noticeDate(LocalDate.now())
                .amount(request.getAmount())
                .reason(request.getReason())
                .build();

        application.setPayLessNotice(payLessNotice);
        application = applicationRepository.save(application);

        log.info("Added pay less notice to application {}", application.getApplicationRef());
        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse addDefaultNotice(Long id) {
        ApplicationForPayment application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationForPayment", id));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new ValidationException("Default notice can only be added to APPROVED applications");
        }

        BigDecimal grossValue = application.getGrossValue() != null ? application.getGrossValue() : BigDecimal.ZERO;
        String defaultRef = "DEFAULT-" + application.getApplicationRef();

        PaymentNotice paymentNotice = PaymentNotice.builder()
                .application(application)
                .noticeDate(LocalDate.now())
                .amount(grossValue)
                .reference(defaultRef)
                .build();

        paymentNoticeRepository.save(paymentNotice);

        log.info("Added default payment notice to application {}", application.getApplicationRef());
        return mapToResponse(application);
    }

    private LocalDate calculateDueDate(LocalDate periodEnd) {
        if (periodEnd == null) {
            return LocalDate.now().plusDays(30);
        }
        return periodEnd.plusDays(30);
    }

    private ApplicationResponse mapToResponse(ApplicationForPayment application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .applicationRef(application.getApplicationRef())
                .applicationNumber(application.getApplicationNumber())
                .contractId(application.getContract() != null ? application.getContract().getId() : null)
                .contractRef(application.getContract() != null ? application.getContract().getContractRef() : null)
                .applicationPeriodStart(application.getApplicationPeriodStart())
                .applicationPeriodEnd(application.getApplicationPeriodEnd())
                .dueDate(application.getDueDate())
                .valueOfWorks(application.getValueOfWorks())
                .retention(application.getRetention())
                .grossValue(application.getGrossValue())
                .status(application.getStatus() != null ? application.getStatus().name() : null)
                .submittedDate(application.getSubmittedDate())
                .paidDate(application.getPaidDate())
                .payerRef(application.getPayerRef())
                .build();
    }
}
