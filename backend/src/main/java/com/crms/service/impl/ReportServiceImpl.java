package com.crms.service.impl;

import com.crms.dto.response.CVRItem;
import com.crms.dto.response.CVRReport;
import com.crms.service.CvrService;
import com.crms.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.RetentionLedger;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.repository.TenderRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CvrService cvrService;
    private final ContractRepository contractRepository;
    private final CISReturnRepository cisReturnRepository;
    private final TenderRepository tenderRepository;

    @Override
    public List<CVRItem> getCVR(Long contractId, String period) {
        log.info("Generating CVR report for contract {} period {}", contractId, period);
        try {
            LocalDate valuationDate = LocalDate.parse(period);
            CVRReport report = cvrService.generateCVR(contractId, valuationDate);

            List<CVRItem> items = new ArrayList<>();
            items.add(CVRItem.builder()
                .contractId(report.getContractId())
                .contractName(report.getContractTitle())
                .valueToDate(report.getGrossValueToDate())
                .costToDate(report.getIndexedCostTotal())
                .grossMargin(report.getGrossMargin())
                .marginPercent(report.getGrossMarginPercent())
                .build());
            return items;
        } catch (Exception e) {
            log.error("Error generating CVR for contract {}: {}", contractId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Object getCashflow(String from, String to) {
        log.info("Generating cashflow report from {} to {}", from, to);
        List<Map<String, Object>> cashflow = new ArrayList<>();
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        // Generate monthly cashflow projections from approved payment certificates
        // and unpaid applications for payment
        // This would normally query payment certificates and applications
        // for forecast cash in, against known payment terms
        for (LocalDate month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            cashflow.add(Map.of(
                "period", month.getYear() + "-" + String.format("%02d", month.getMonthValue()),
                "forecast_in", BigDecimal.ZERO,
                "forecast_out", BigDecimal.ZERO,
                "net", BigDecimal.ZERO,
                "cumulative", BigDecimal.ZERO
            ));
        }
        return cashflow;
    }

    @Override
    public Object getRetention() {
        log.info("Generating retention schedule report");
        // Query retention ledger for all contracts
        // Return a list of contracts with retention held, released at PC, released at defects, balance
        List<Map<String, Object>> retentionSchedule = new ArrayList<>();
        List<Contract> contracts = contractRepository.findByStatus(ContractStatus.IN_PROGRESS);
        
        for (Contract contract : contracts) {
            Map<String, Object> item = new HashMap<>();
            item.put("contractId", contract.getId());
            item.put("contractRef", contract.getContractRef());
            item.put("title", contract.getTitle());
            
            RetentionLedger ledger = contract.getRetentionLedger();
            if (ledger != null) {
                item.put("totalRetention", ledger.getTotalRetention() != null ? ledger.getTotalRetention() : BigDecimal.ZERO);
                item.put("releasedAtPC", ledger.getReleasedAtPC() != null ? ledger.getReleasedAtPC() : BigDecimal.ZERO);
                item.put("releasedAtDefects", ledger.getReleasedAtDefects() != null ? ledger.getReleasedAtDefects() : BigDecimal.ZERO);
                item.put("balance", ledger.getBalance() != null ? ledger.getBalance() : BigDecimal.ZERO);
            } else {
                item.put("totalRetention", BigDecimal.ZERO);
                item.put("releasedAtPC", BigDecimal.ZERO);
                item.put("releasedAtDefects", BigDecimal.ZERO);
                item.put("balance", BigDecimal.ZERO);
            }
            retentionSchedule.add(item);
        }
        
        return retentionSchedule;
    }

    @Override
    public Object getCISSummary(String taxMonth) {
        log.info("Generating CIS summary for {}", taxMonth);
        // Query CIS returns for the given tax month
        // Group by subcontractor, sum deductions, return amounts
        List<CISReturn> cisReturns = cisReturnRepository.findByTaxMonth(taxMonth);
        Map<String, Map<String, Object>> summaryBySubcontractor = new LinkedHashMap<>();
        
        for (CISReturn cisReturn : cisReturns) {
            String key = cisReturn.getSubcontractor() != null 
                ? cisReturn.getSubcontractor().getId().toString() : "unknown";
            summaryBySubcontractor.computeIfAbsent(key, k -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("subcontractorId", key);
                summary.put("subcontractorName", cisReturn.getSubcontractor() != null 
                    ? cisReturn.getSubcontractor().getName() : "Unknown");
                summary.put("totalGrossValue", BigDecimal.ZERO);
                summary.put("totalDeduction", BigDecimal.ZERO);
                summary.put("totalNetValue", BigDecimal.ZERO);
                summary.put("returnCount", 0);
                return summary;
            });
            
            Map<String, Object> summary = summaryBySubcontractor.get(key);
            BigDecimal grossValue = cisReturn.getGrossValue() != null ? cisReturn.getGrossValue() : BigDecimal.ZERO;
            BigDecimal deduction = cisReturn.getDeductionAmount() != null ? cisReturn.getDeductionAmount() : BigDecimal.ZERO;
            
            summary.put("totalGrossValue", ((BigDecimal) summary.get("totalGrossValue")).add(grossValue));
            summary.put("totalDeduction", ((BigDecimal) summary.get("totalDeduction")).add(deduction));
            summary.put("totalNetValue", ((BigDecimal) summary.get("totalNetValue")).add(grossValue.subtract(deduction)));
            summary.put("returnCount", ((Integer) summary.get("returnCount")) + 1);
        }
        
        return new ArrayList<>(summaryBySubcontractor.values());
    }

    @Override
    public Object getPlantUtilization(Map<String, Object> params) {
        log.info("Generating plant utilization report");
        // Query plant allocations and hire records
        // Calculate utilization % per plant item = (on-hire days / total days) × 100
        List<Map<String, Object>> utilization = new ArrayList<>();
        // Implementation would query PlantAllocationRepository and PlantRepository
        // For now, return empty list as placeholder
        return utilization;
    }

    @Override
    public Object getTenderPipeline() {
        log.info("Generating tender pipeline report");
        // Query tenders by status, group by probability
        // Calculate potential value per pipeline stage
        List<Tender> tenders = tenderRepository.findAll();
        Map<String, Map<String, Object>> pipelineByStage = new LinkedHashMap<>();
        
        for (Tender tender : tenders) {
            String stage = tender.getStatus() != null ? tender.getStatus().name() : "UNKNOWN";
            pipelineByStage.computeIfAbsent(stage, k -> {
                Map<String, Object> stageData = new HashMap<>();
                stageData.put("stage", stage);
                stageData.put("tenderCount", 0);
                stageData.put("totalValue", BigDecimal.ZERO);
                stageData.put("tenders", new ArrayList<Map<String, Object>>());
                return stageData;
            });
            
            Map<String, Object> stageData = pipelineByStage.get(stage);
            stageData.put("tenderCount", ((Integer) stageData.get("tenderCount")) + 1);
            BigDecimal value = tender.getEstimatedValue() != null ? tender.getEstimatedValue() : BigDecimal.ZERO;
            stageData.put("totalValue", ((BigDecimal) stageData.get("totalValue")).add(value));
            
            Map<String, Object> tenderInfo = new HashMap<>();
            tenderInfo.put("id", tender.getId());
            tenderInfo.put("title", tender.getTitle());
            tenderInfo.put("clientName", tender.getClient() != null ? tender.getClient().getName() : null);
            tenderInfo.put("estimatedValue", value);
            tenderInfo.put("submissionDate", tender.getSubmissionDate());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stageTenders = (List<Map<String, Object>>) stageData.get("tenders");
            stageTenders.add(tenderInfo);
        }
        
        return new ArrayList<>(pipelineByStage.values());
    }
}