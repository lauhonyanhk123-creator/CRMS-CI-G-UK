package com.crms.service.impl;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.PayLessNotice;
import com.crms.domain.contract.entity.PaymentNotice;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.DeadlineStatus;
import com.crms.domain.contract.enums.NoticeType;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.PayLessNoticeRepository;
import com.crms.domain.contract.repository.PaymentNoticeRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.company.enums.CisStatus;
import com.crms.domain.company.repository.CompanyRepository;
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
import java.time.LocalDateTime;
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
    private final CompanyRepository companyRepository;

    @Override
    public PageResponse<ApplicationResponse> findByContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        List<ApplicationForPayment> applications = applicationRepository.findByContractIdOrderByApplicationPeriodEndDesc(contractId);
        if (applications == null) {
            applications = applicationRepository.findByContractId(contractId);
        }

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
        String applicationRef = contract.getContractRef() + "-" + applicationNumber;

        ApplicationForPayment application = ApplicationForPayment.builder()
                .contract(contract)
                .applicationRef(applicationRef)
                .applicationNumber(applicationNumber)
                .applicationPeriodStart(request.getApplicationPeriodStart())
                .applicationPeriodEnd(request.getApplicationPeriodEnd())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : calculateDueDate(request.getApplicationPeriodEnd()))
                .valueOfWorks(request.getValueOfWorks())
                .retention(request.getRetention() != null ? request.getRetention() : calculateRetention(contract, request.getValueOfWorks()))
                .status(ApplicationStatus.DRAFT)
                .reverseCharge(calculateReverseCharge(contract))
                .build();

        // ================================================================
        // C) Calculate Pay-Less Notice Deadline on Create
        // Under s.111 of the Housing Grants, Construction and Regeneration Act 1996
        // The deadline is set to 5 days after the application period end
        // ================================================================
        application.setPayLessNoticeDeadline(
            request.getApplicationPeriodEnd().plusDays(5));  // 5 days after period end per s.111

        application.onSave();
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

        // ================================================================
        // A) CIS Verification Gate - Check all subcontractors are verified
        // ================================================================
        Contract contract = application.getContract();
        if (companyRepository != null) {
            List<Company> subcontractors = companyRepository.findByContractId(contract.getId());
            for (Company sub : subcontractors) {
                if (sub.getCisStatus() != CisStatus.VERIFIED) {
                    throw new ValidationException(
                        "Subcontractor '" + sub.getName() + "' is not CIS verified. " +
                        "CIS deductions cannot be applied until HMRC verification is complete.");
                }
            }
        }

        // ================================================================
        // B) Pay-Less Notice Deadline Enforcement (s.111)
        // Under s.111 of the Housing Grants, Construction and Regeneration Act 1996
        // ================================================================
        if (application.getPayLessNoticeDeadline() != null) {
            if (application.getPayLessNoticeDeadline().isBefore(LocalDate.now())) {
                throw new ValidationException(
                    "Pay-less notice deadline has passed (" + application.getPayLessNoticeDeadline() + 
                    "). This application cannot be submitted under s.111 of the Construction Act 1996.");
            }
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedDate(LocalDate.now());
        application.setPayerRef("PAY-" + application.getApplicationRef());

        BigDecimal grossValue = application.getGrossValue() != null ? application.getGrossValue() : BigDecimal.ZERO;
        PaymentNotice paymentNotice = PaymentNotice.builder()
                .application(application)
                .noticeType(NoticeType.PAYMENT)
                .noticeDate(LocalDate.now())
                .amount(grossValue)
                .sumConsideredDue(grossValue)
                .currency("GBP")
                .reference(application.getPayerRef())
                .issuedOn(LocalDateTime.now())
                .finalDateForPayment(LocalDateTime.now().plusDays(30))
                .build();
        paymentNoticeRepository.save(paymentNotice);

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
                .noticeType(NoticeType.PAYMENT)
                .noticeDate(LocalDate.now())
                .amount(request.getAmount())
                .sumConsideredDue(request.getAmount())
                .currency("GBP")
                .reference(request.getReference())
                .issuedOn(LocalDateTime.now())
                .finalDateForPayment(LocalDateTime.now().plusDays(30))
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
                .noticeType(NoticeType.DEFAULT_PAYMENT_NOTICE)
                .noticeDate(LocalDate.now())
                .amount(grossValue)
                .sumConsideredDue(grossValue)
                .currency("GBP")
                .reference(defaultRef)
                .issuedOn(LocalDateTime.now())
                .finalDateForPayment(LocalDateTime.now().plusDays(30))
                .build();

        paymentNoticeRepository.save(paymentNotice);

        log.info("Added default payment notice to application {}", application.getApplicationRef());
        return mapToResponse(application);
    }

    private BigDecimal calculateRetention(Contract contract, BigDecimal valueOfWorks) {
        if (valueOfWorks == null) {
            return null;
        }
        BigDecimal percentage = contract.getRetentionPercent() != null ? contract.getRetentionPercent() : BigDecimal.ZERO;
        return valueOfWorks.multiply(percentage).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    private LocalDate calculateDueDate(LocalDate periodEnd) {
        if (periodEnd == null) {
            return LocalDate.now().plusDays(30);
        }
        return periodEnd.plusDays(30);
    }

    /**
     * Calculates the pay-less notice s.111 deadline.
     * Under s.111 of the Housing Grants, Construction and Regeneration Act 1996,
     * the pay-less notice deadline is 5 days before the application date/submitted date.
     * 
     * @param applicationDate the date the application was submitted
     * @return the deadline for serving a pay-less notice, or null if no application date
     */
    public LocalDate calculatePayLessNoticeDeadline(LocalDate applicationDate) {
        if (applicationDate == null) {
            return null;
        }
        return applicationDate.minusDays(5);
    }

    /**
     * Calculates the deadline status based on the pay-less notice deadline.
     * 
     * @param deadline the pay-less notice deadline
     * @return the current deadline status
     */
    public DeadlineStatus calculateDeadlineStatus(LocalDate deadline) {
        if (deadline == null) {
            return DeadlineStatus.NO_DEADLINE;
        }
        
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, deadline);
        
        if (daysRemaining < 0) {
            return DeadlineStatus.DEADLINE_PASSED;
        } else if (daysRemaining <= 2) {
            return DeadlineStatus.DEADLINE_APPROACHING;
        } else {
            return DeadlineStatus.DEADLINE_ACTIVE;
        }
    }

    /**
     * Calculates VAT reverse charge flag based on UK construction industry rules.
     * Reverse charge applies when:
     * - Subcontractor is VAT registered (has vatNumber)
     * - Contract value exceeds the VAT threshold (currently £85,000 for 2025/26)
     */
    private Boolean calculateReverseCharge(Contract contract) {
        if (contract == null) {
            return false;
        }

        // Get the subcontractor from the tender/client relationship
        // In this system, the subcontractor is typically linked via tender
        if (contract.getTender() != null && contract.getTender().getClient() != null) {
            var client = contract.getTender().getClient();
            String vatNumber = client.getVatNumber();

            // Check if subcontractor is VAT registered
            boolean isVatRegistered = vatNumber != null && !vatNumber.isBlank();

            // Check if contract value exceeds VAT threshold
            // UK VAT reverse charge threshold for construction is £85,000 (2025/26)
            BigDecimal vatThreshold = new BigDecimal("85000");
            BigDecimal contractValue = contract.getContractValue();
            boolean exceedsThreshold = contractValue != null && contractValue.compareTo(vatThreshold) >= 0;

            return isVatRegistered && exceedsThreshold;
        }

        return false;
    }

    private ApplicationResponse mapToResponse(ApplicationForPayment application) {
        // Calculate pay-less notice s.111 deadline based on submitted date
        LocalDate submittedDate = application.getSubmittedDate();
        LocalDate payLessNoticeDeadline = calculatePayLessNoticeDeadline(submittedDate);
        DeadlineStatus deadlineStatus = calculateDeadlineStatus(payLessNoticeDeadline);
        
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
                .reverseCharge(application.getReverseCharge())
                .payLessNoticeDeadline(payLessNoticeDeadline)
                .deadlineStatus(deadlineStatus)
                .build();
    }
}
