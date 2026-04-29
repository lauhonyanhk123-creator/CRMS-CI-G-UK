package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.financial.entity.WipReport;
import com.crms.domain.financial.entity.WipReport.WipReportStatus;
import com.crms.domain.financial.repository.WipReportRepository;
import com.crms.dto.request.WipReportRequest;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WipJournalServiceImpl implements WipJournalService {

    private final WipReportRepository wipReportRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public WipReportResponse generateWipReport(Long contractId, LocalDate reportDate) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        // Check if report already exists for this date
        if (wipReportRepository.existsByContractIdAndReportDate(contractId, reportDate)) {
            WipReport existing = wipReportRepository.findByContractIdAndReportDate(contractId, reportDate)
                    .orElseThrow();
            return mapToResponse(existing);
        }

        // Calculate period (assuming monthly)
        LocalDate periodStart = reportDate.withDayOfMonth(1);
        LocalDate periodEnd = reportDate.withDayOfMonth(reportDate.lengthOfMonth());

        // Create new WIP report with default/calculated values
        WipReport wipReport = WipReport.builder()
                .contract(contract)
                .reportDate(reportDate)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .certifiedValue(BigDecimal.ZERO)
                .costIncurred(BigDecimal.ZERO)
                .wipValue(BigDecimal.ZERO)
                .underRecovery(BigDecimal.ZERO)
                .overRecovery(BigDecimal.ZERO)
                .status(WipReportStatus.DRAFT)
                .build();

        wipReport.calculateWip();
        wipReport = wipReportRepository.save(wipReport);

        log.info("Generated WIP report {} for contract {} dated {}", wipReport.getId(), contractId, reportDate);
        return mapToResponse(wipReport);
    }

    @Override
    @Transactional
    public List<WipReportResponse> generateAllWipReports(LocalDate reportDate) {
        List<Contract> contracts = contractRepository.findAll();

        return contracts.stream()
                .filter(contract -> contract.getStatus() == com.crms.domain.contract.enums.ContractStatus.ACTIVE)
                .map(contract -> {
                    try {
                        return generateWipReport(contract.getId(), reportDate);
                    } catch (Exception e) {
                        log.warn("Failed to generate WIP report for contract {}: {}", contract.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Override
    public WipReportResponse getWipReportById(Long reportId) {
        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WipReport", reportId));
        return mapToResponse(report);
    }

    @Override
    public PageResponse<WipReportResponse> getWipReportHistory(Long contractId, int page, int size) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reportDate"));
        Page<WipReport> reportPage = wipReportRepository.findByContractId(contractId, pageable);

        List<WipReportResponse> content = reportPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<WipReportResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(reportPage.getTotalElements())
                .totalPages(reportPage.getTotalPages())
                .build();
    }

    @Override
    public List<WipReportResponse> getWipReportsByDate(LocalDate reportDate) {
        List<WipReport> reports = wipReportRepository.findByContractIdOrderByReportDateDesc(null);

        // Find reports by date - need to filter manually as there's no direct method
        List<WipReport> allReports = wipReportRepository.findAll().stream()
                .filter(r -> r.getReportDate().equals(reportDate))
                .collect(Collectors.toList());

        return allReports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WipReportResponse postToJournal(Long reportId) {
        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WipReport", reportId));

        if (report.getStatus() != WipReportStatus.DRAFT) {
            throw new ValidationException("Only DRAFT reports can be posted to journal");
        }

        // Generate journal reference
        String journalRef = "WIP-" + report.getContract().getContractRef() + "-" + 
                           report.getReportDate().toString().replace("-", "") + "-" + 
                           UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        report.setJournalReference(journalRef);
        report.setStatus(WipReportStatus.POSTED);

        // Record posting timestamp
        report.setUpdatedAt(java.time.LocalDateTime.now());

        report = wipReportRepository.save(report);

        log.info("Posted WIP report {} to journal with reference {}", reportId, journalRef);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public WipReportResponse reverseJournal(Long reportId) {
        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WipReport", reportId));

        if (report.getStatus() != WipReportStatus.POSTED) {
            throw new ValidationException("Only POSTED reports can be reversed");
        }

        // Store old journal reference for audit
        String oldJournalRef = report.getJournalReference();

        // Generate reversal reference
        String reversalRef = oldJournalRef + "-REV";

        report.setJournalReference(reversalRef);
        report.setStatus(WipReportStatus.REVERSED);
        report.setUpdatedAt(java.time.LocalDateTime.now());

        report = wipReportRepository.save(report);

        log.info("Reversed WIP report {} - new reference {}", reportId, reversalRef);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public void deleteDraftReport(Long reportId) {
        WipReport report = wipReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("WipReport", reportId));

        if (report.getStatus() != WipReportStatus.DRAFT) {
            throw new ValidationException("Only DRAFT reports can be deleted");
        }

        wipReportRepository.delete(report);
        log.info("Deleted draft WIP report {}", reportId);
    }

    private WipReportResponse mapToResponse(WipReport report) {
        return WipReportResponse.builder()
                .id(report.getId())
                .contractId(report.getContract() != null ? report.getContract().getId() : null)
                .contractRef(report.getContract() != null ? report.getContract().getContractRef() : null)
                .reportDate(report.getReportDate())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .certifiedValue(report.getCertifiedValue())
                .costIncurred(report.getCostIncurred())
                .wipValue(report.getWipValue())
                .underRecovery(report.getUnderRecovery())
                .overRecovery(report.getOverRecovery())
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .journalReference(report.getJournalReference())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
