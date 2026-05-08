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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.RetentionLedger;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.domain.operative.repository.TimesheetRepository;
import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.entity.PlantItem;
import com.crms.domain.plant.repository.PlantAllocationRepository;
import com.crms.domain.plant.repository.PlantItemRepository;
import com.crms.dto.response.CITBLevyReport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CvrService cvrService;
    private final ContractRepository contractRepository;
    private final ApplicationForPaymentRepository applicationRepository;
    private final CISReturnRepository cisReturnRepository;
    private final TenderRepository tenderRepository;
    private final TimesheetRepository timesheetRepository;
    private final PlantItemRepository plantItemRepository;
    private final PlantAllocationRepository plantAllocationRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object getCashflow(String from, String to) {
        log.info("Generating cashflow report from {} to {}", from, to);

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        // Initialise a zero-value bucket for every month in the range
        Map<String, BigDecimal> inByMonth = new LinkedHashMap<>();
        for (LocalDate m = start.withDayOfMonth(1); !m.isAfter(end.withDayOfMonth(1)); m = m.plusMonths(1)) {
            inByMonth.put(m.getYear() + "-" + String.format("%02d", m.getMonthValue()), BigDecimal.ZERO);
        }

        // PAID applications: use paidDate as the actual cash-in date
        // APPROVED/SUBMITTED applications: use dueDate as the forecast cash-in date
        for (ApplicationForPayment app : applicationRepository.findCashflowRelevantByDateRange(start, end)) {
            LocalDate effectiveDate = app.getStatus() == ApplicationStatus.PAID
                    ? app.getPaidDate() : app.getDueDate();
            if (effectiveDate == null) continue;
            String key = effectiveDate.getYear() + "-" + String.format("%02d", effectiveDate.getMonthValue());
            if (inByMonth.containsKey(key)) {
                BigDecimal gross = app.getGrossValue() != null ? app.getGrossValue() : BigDecimal.ZERO;
                inByMonth.merge(key, gross, BigDecimal::add);
            }
        }

        List<Map<String, Object>> cashflow = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : inByMonth.entrySet()) {
            BigDecimal forecastIn = entry.getValue().setScale(2, RoundingMode.HALF_UP);
            cumulative = cumulative.add(forecastIn);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", entry.getKey());
            row.put("forecast_in", forecastIn);
            row.put("forecast_out", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            row.put("net", forecastIn);
            row.put("cumulative", cumulative.setScale(2, RoundingMode.HALF_UP));
            cashflow.add(row);
        }
        return cashflow;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object getCISSummary(String taxMonth) {
        log.info("Generating CIS summary for {}", taxMonth);
        // Query CIS returns for the given tax month
        // Group by subcontractor, sum deductions, return amounts
        List<CISReturn> cisReturns = cisReturnRepository.findByTaxMonth(taxMonth).map(java.util.List::of).orElse(java.util.Collections.emptyList());
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object getPlantUtilization(Map<String, Object> params) {
        log.info("Generating plant utilization report");

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(30);
        if (params != null) {
            if (params.containsKey("from")) fromDate = LocalDate.parse(params.get("from").toString());
            if (params.containsKey("to")) toDate = LocalDate.parse(params.get("to").toString());
        }
        final LocalDate from = fromDate;
        final LocalDate to = toDate;
        long periodDays = ChronoUnit.DAYS.between(from, to) + 1;

        List<Map<String, Object>> utilization = new ArrayList<>();
        for (PlantItem plant : plantItemRepository.findAll(org.springframework.data.domain.Sort.by("plantRef"))) {
            List<PlantAllocation> allocations =
                    plantAllocationRepository.findByPlantIdAndDateRange(plant.getId(), from, to);

            long onHireDays = 0;
            for (PlantAllocation alloc : allocations) {
                LocalDate allocStart = alloc.getStartDate() != null && alloc.getStartDate().isAfter(from)
                        ? alloc.getStartDate() : from;
                LocalDate allocEnd = alloc.getEndDate() == null || alloc.getEndDate().isAfter(to)
                        ? to : alloc.getEndDate();
                if (!allocStart.isAfter(allocEnd)) {
                    onHireDays += ChronoUnit.DAYS.between(allocStart, allocEnd) + 1;
                }
            }

            double utilisationPct = periodDays > 0 ? (double) onHireDays / periodDays * 100 : 0;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("plantId", plant.getId());
            item.put("plantRef", plant.getPlantRef());
            item.put("description", plant.getDescription());
            item.put("status", plant.getStatus() != null ? plant.getStatus().name() : "");
            item.put("onHireDays", onHireDays);
            item.put("periodDays", periodDays);
            item.put("utilisationPercent", Math.round(utilisationPct * 10.0) / 10.0);
            utilization.add(item);
        }
        return utilization;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object getTenderPipeline() {
        log.info("Generating tender pipeline report");
        // Query tenders by status, group by probability
        // Calculate potential value per pipeline stage
        List<Tender> tenders = tenderRepository.findByStatusIn(java.util.Arrays.asList(com.crms.domain.tender.enums.TenderStatus.values()));
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

    private static final BigDecimal CITB_LEVY_RATE = new BigDecimal("0.005"); // 0.5%

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Object getCITBLevy(String period) {
        log.info("Generating CITB Levy report for period {}", period);
        
        try {
            // Parse period - expected format: YYYY-MM or YYYY-QN
            LocalDate periodStart;
            LocalDate periodEnd;
            
            if (period.contains("Q")) {
                // Quarterly format: YYYY-Q1, YYYY-Q2, etc.
                String[] parts = period.split("-Q");
                int year = Integer.parseInt(parts[0]);
                int quarter = Integer.parseInt(parts[1]);
                periodStart = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
                periodEnd = periodStart.plusMonths(3).minusDays(1);
            } else {
                // Monthly format: YYYY-MM - use the entire month
                LocalDate parsed = LocalDate.parse(period + "-01");
                periodStart = parsed.withDayOfMonth(1);
                periodEnd = parsed.withDayOfMonth(parsed.lengthOfMonth());
            }

            List<CITBLevyReport> reports = new ArrayList<>();
            List<Contract> contracts = contractRepository.findContractsActiveDuring(periodStart, periodEnd);

            for (Contract contract : contracts) {
                CITBLevyReport report = calculateCITBLevyForContract(contract, periodStart, periodEnd);
                reports.add(report);
            }

            return reports;
        } catch (Exception e) {
            log.error("Error generating CITB Levy report for period {}: {}", period, e.getMessage());
            return new ArrayList<>();
        }
    }

    private CITBLevyReport calculateCITBLevyForContract(Contract contract, LocalDate periodStart, LocalDate periodEnd) {
        Long siteId = contract.getSite() != null ? contract.getSite().getId() : null;
        
        // Try to get live wage data from timesheets
        BigDecimal operativeWages = BigDecimal.ZERO;
        boolean usedLiveWageData = false;
        
        if (siteId != null) {
            try {
                BigDecimal wages = timesheetRepository.calculateTotalWagesBySiteAndPeriod(siteId, periodStart, periodEnd);
                if (wages != null && wages.compareTo(BigDecimal.ZERO) > 0) {
                    operativeWages = wages;
                    usedLiveWageData = true;
                }
            } catch (Exception e) {
                log.warn("Could not retrieve timesheet data for site {}: {}", siteId, e.getMessage());
            }
        }

        // Get contract labour value as fallback
        BigDecimal contractLabourValue = contract.getLabourValue() != null ? contract.getLabourValue() : BigDecimal.ZERO;

        // Determine which labour value to use
        BigDecimal labourCostsUsed;
        if (usedLiveWageData) {
            labourCostsUsed = operativeWages;
        } else {
            // Fallback to contract labour value (pro-rated if needed based on period)
            labourCostsUsed = contractLabourValue;
        }

        // Calculate CITB Levy = 0.5% of labour costs
        BigDecimal citbLevy = labourCostsUsed.multiply(CITB_LEVY_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        return CITBLevyReport.builder()
                .contractId(contract.getId())
                .contractRef(contract.getContractRef())
                .contractTitle(contract.getTitle())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .operativeWages(operativeWages)
                .contractLabourValue(contractLabourValue)
                .labourCostsUsed(labourCostsUsed)
                .usedLiveWageData(usedLiveWageData)
                .qualifyingLabourCosts(labourCostsUsed)
                .levyRate(CITB_LEVY_RATE)
                .citbLevy(citbLevy)
                .contractValue(contract.getContractValue())
                .build();
    }
}