package com.crms.service.impl;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.financial.entity.JournalEntry;
import com.crms.domain.financial.entity.WipReport;
import com.crms.domain.financial.repository.CostTransactionRepository;
import com.crms.domain.financial.repository.JournalEntryRepository;
import com.crms.domain.financial.repository.WipReportRepository;
import com.crms.dto.response.WipReportResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.WipJournalService;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WipJournalServiceImpl implements WipJournalService {

    private final WipReportRepository wipReportRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final CostTransactionRepository costTransactionRepository;
    private final ContractRepository contractRepository;
    private final ApplicationForPaymentRepository applicationForPaymentRepository;

    private static final DateTimeFormatter JOURNAL_REF_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String WIP_DEBIT_ACCOUNT = "2100";
    private static final String WIP_CREDIT_ACCOUNT = "3100";
    private static final String COST_DEBIT_ACCOUNT = "4100";
    private static final String COST_CREDIT_ACCOUNT = "2100";
    private static final String REVENUE_DEBIT_ACCOUNT = "3100";
    private static final String REVENUE_CREDIT_ACCOUNT = "8100";

    @Override
    @Transactional
    public WipReportResponse generateWipReport(Long contractId, LocalDate reportDate) {
        log.info("Generating WIP report for contract {} on date {}", contractId, reportDate);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new ValidationException("Contract must be ACTIVE to generate WIP report");
        }

        if (wipReportRepository.existsByContractIdAndReportDate(contractId, reportDate)) {
            throw new ValidationException("WIP report already exists for contract " + contractId + " on " + reportDate);
        }

        YearMonth yearMonth = YearMonth.from(reportDate);
        LocalDate periodStart = yearMonth.atDay(1);
        LocalDate periodEnd = yearMonth.atEndOfMonth();

        BigDecimal certifiedValue = calculateCertifiedValue(contractId, periodEnd);
        BigDecimal costIncurred = calculateCostIncurred(contractId, periodEnd);

        WipReport wipReport = WipReport.builder()
                .contract(contract)
                .reportDate(reportDate)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .certifiedValue(certifiedValue)
                .costIncurred(costIncurred)
                .status(WipReport.WipReportStatus.DRAFT)
                .build();

        wipReport.calculateWip();
        WipReport savedReport = wipReportRepository.save(wipReport);

        log.info("WIP report generated successfully: {}", savedReport.getId());
        return mapToResponse(savedReport);
    }

    @Override
    @Transactional
    public List<WipReportResponse> generateAllWipReports(LocalDate reportDate) {
        log.info("Generating WIP reports for all active contracts on date {}", reportDate);

        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);

        List<WipReportResponse> results = activeContracts.stream()
                .filter(contract -> !wipReportRepository.existsByContractIdAndReportDate(contract.getId(), reportDate))
                .map(contract -> {
                    try {
                        return generateWipReport(contract.getId(), reportDate);
                    } catch (Exception e) {
                        log.error("Failed to generate WIP report for contract {}: {}", contract.getId(), e.getMessage());
                        return null;
                    }
                })
                .collect(Collectors.toList());

        log.info("Generated {} WIP reports out of {} active contracts", results.size(), activeContracts.size());
        return results;
    }

    @Override
    public WipReportResponse getWipReportById(Long reportId) {
        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WIP Report not found with id: " + reportId));
        return mapToResponse(report);
    }

    @Override
    public PageResponse<WipReportResponse> getWipReportHistory(Long contractId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reportDate"));
        Page<WipReport> reportPage = wipReportRepository.findByContractId(contractId, pageable);
        return mapToPageResponse(reportPage);
    }

    @Override
    public List<WipReportResponse> getWipReportsByDate(LocalDate reportDate) {
        List<WipReport> reports = wipReportRepository.findDraftsByReportDate(reportDate);
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WipReportResponse postToJournal(Long reportId) {
        log.info("Posting WIP report {} to journal", reportId);

        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WIP Report not found with id: " + reportId));

        if (report.getStatus() != WipReport.WipReportStatus.DRAFT) {
            throw new ValidationException("Only DRAFT reports can be posted");
        }

        String journalRef = generateJournalReference(report);

        createWipJournalEntries(report, journalRef);

        report.setStatus(WipReport.WipReportStatus.POSTED);
        report.setJournalReference(journalRef);
        WipReport savedReport = wipReportRepository.save(report);

        log.info("WIP report {} posted successfully with journal reference {}", reportId, journalRef);
        return mapToResponse(savedReport);
    }

    @Override
    @Transactional
    public WipReportResponse reverseJournal(Long reportId) {
        log.info("Reversing journal entries for WIP report {}", reportId);

        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WIP Report not found with id: " + reportId));

        if (report.getStatus() != WipReport.WipReportStatus.POSTED) {
            throw new ValidationException("Only POSTED reports can be reversed");
        }

        List<JournalEntry> entries = journalEntryRepository.findByWipReportId(reportId);
        entries.forEach(entry -> entry.setStatus(JournalEntry.JournalStatus.REVERSED));
        journalEntryRepository.saveAll(entries);

        report.setStatus(WipReport.WipReportStatus.REVERSED);
        WipReport savedReport = wipReportRepository.save(report);

        log.info("Journal entries reversed for WIP report {}", reportId);
        return mapToResponse(savedReport);
    }

    @Override
    @Transactional
    public void deleteDraftReport(Long reportId) {
        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WIP Report not found with id: " + reportId));

        if (report.getStatus() != WipReport.WipReportStatus.DRAFT) {
            throw new ValidationException("Only DRAFT reports can be deleted");
        }

        wipReportRepository.delete(report);
        log.info("Draft WIP report {} deleted", reportId);
    }

    private BigDecimal calculateCertifiedValue(Long contractId, LocalDate upToDate) {
        List<ApplicationForPayment> applications = applicationForPaymentRepository
                .findApprovedApplicationsUpToDate(contractId, upToDate);

        return applications.stream()
                .map(ApplicationForPayment::getGrossValue)
                .filter(val -> val != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCostIncurred(Long contractId, LocalDate upToDate) {
        return costTransactionRepository.sumAmountByContractIdUpToDate(contractId, upToDate);
    }

    private String generateJournalReference(WipReport report) {
        return "WIP-" + report.getReportDate().format(JOURNAL_REF_FORMAT) + "-" + 
               report.getContract().getContractRef() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createWipJournalEntries(WipReport report, String journalRef) {
        LocalDate transactionDate = report.getReportDate();
        String description = "WIP Journal - " + report.getContract().getContractRef() + 
                             " - Period: " + report.getPeriodStart() + " to " + report.getPeriodEnd();

        if (report.getWipValue().compareTo(BigDecimal.ZERO) > 0) {
            createJournalEntry(report, journalRef, transactionDate,
                    description + " - Over Recovery",
                    WIP_DEBIT_ACCOUNT, WIP_CREDIT_ACCOUNT,
                    report.getWipValue(), report.getWipValue());
        } else if (report.getWipValue().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal underAmount = report.getWipValue().abs();
            createJournalEntry(report, journalRef, transactionDate,
                    description + " - Under Recovery",
                    COST_DEBIT_ACCOUNT, WIP_CREDIT_ACCOUNT,
                    underAmount, underAmount);
        }

        if (report.getCertifiedValue().compareTo(BigDecimal.ZERO) > 0) {
            createJournalEntry(report, journalRef, transactionDate,
                    description + " - Revenue Recognition",
                    REVENUE_DEBIT_ACCOUNT, REVENUE_CREDIT_ACCOUNT,
                    report.getCertifiedValue(), report.getCertifiedValue());
        }
    }

    private void createJournalEntry(WipReport report, String journalRef, LocalDate transactionDate,
                                   String description, String debitAccount, String creditAccount,
                                   BigDecimal debitAmount, BigDecimal creditAmount) {
        JournalEntry entry = JournalEntry.builder()
                .wipReport(report)
                .journalReference(journalRef)
                .journalType("WIP_JOURNAL")
                .transactionDate(transactionDate)
                .description(description)
                .debitAccountCode(debitAccount)
                .creditAccountCode(creditAccount)
                .debitAmount(debitAmount)
                .creditAmount(creditAmount)
                .status(JournalEntry.JournalStatus.POSTED)
                .build();

        journalEntryRepository.save(entry);
    }

    private WipReportResponse mapToResponse(WipReport report) {
        return WipReportResponse.builder()
                .id(report.getId())
                .contractId(report.getContract().getId())
                .contractRef(report.getContract().getContractRef())
                .reportDate(report.getReportDate())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .certifiedValue(report.getCertifiedValue())
                .costIncurred(report.getCostIncurred())
                .wipValue(report.getWipValue())
                .underRecovery(report.getUnderRecovery())
                .overRecovery(report.getOverRecovery())
                .status(report.getStatus().name())
                .journalReference(report.getJournalReference())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private PageResponse<WipReportResponse> mapToPageResponse(Page<WipReport> page) {
        List<WipReportResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<WipReportResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
