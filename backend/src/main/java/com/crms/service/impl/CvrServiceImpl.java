package com.crms.service.impl;

import java.util.Optional;
import com.crms.domain.contract.entity.*;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.*;
import com.crms.domain.material.entity.MuckawayTicket;
import com.crms.domain.material.repository.MuckawayTicketRepository;
import com.crms.domain.material.repository.PurchaseOrderRepository;
import com.crms.domain.operative.repository.TimesheetRepository;
import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.repository.PlantAllocationRepository;
import com.crms.dto.response.CVRReport;
import com.crms.dto.response.CVRItem;
import com.crms.repository.BCISIndexRepository;
import com.crms.service.CvrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Cost-Value Reconciliation engine.
 *
 * Calculation methodology (per CESMM4 and NEC4):
 *
 * VALUE SIDE:
 * 1. Measured work = sum(measured quantities × agreed rates)
 * 2. Dayworks = time+materials at agreed daywork rates (NOT BCIS-indexed)
 * 3. Variations = agreed variation orders added to contract sum
 * 4. Gross value = measured + dayworks + variations
 * 5. Less retention = value × retention%
 * 6. Net value = gross value - retention
 *
 * COST SIDE:
 * 1. Materials cost (pre-indexation) = sum of material deliveries for period
 * 2. Plant cost = plant hire records + fuel for period
 * 3. Labour cost = timesheet hours × rate for period
 * 4. Subcontract cost = subcontractor applications/cis returns for period
 * 5. Dayworks cost = direct dayworks (time+materials)
 * 6. Disallowed costs = NEC4 cl.11.2(25) items (client-caused delay, etc.)
 *
 * BCIS INDEXATION (CESMM4 Schedule R):
 * 1. Look up base index at contract start date (Series 3 for materials)
 * 2. Look up current index at valuation date (Series 3)
 * 3. Adjustment factor = currentIndex / baseIndex
 * 4. Indexed materials cost = base materials cost × adjustment factor
 *
 * GROSS MARGIN:
 * 1. Gross margin = gross value to date - indexed cost to date
 * 2. Margin % = gross margin / gross value to date × 100
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CvrServiceImpl implements CvrService {

    private final ContractRepository contractRepository;
    private final ApplicationForPaymentRepository applicationRepository;
    private final PaymentCertificateRepository paymentCertificateRepository;
    private final RetentionLedgerRepository retentionLedgerRepository;
    private final RetentionMovementRepository retentionMovementRepository;
    private final BCISIndexRepository bcisIndexRepository;
    private final MuckawayTicketRepository muckawayTicketRepository;
    private final TimesheetRepository timesheetRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PlantAllocationRepository plantAllocationRepository;

    // BCIS Series 3 = All-in Materials Index (used for Schedule R indexation)
    private static final int BCIS_MATERIALS_SERIES = 3;
    // Default fallback indices if no BCIS data in database
    private static final BigDecimal DEFAULT_BASE_INDEX = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_CURRENT_INDEX = new BigDecimal("142.30");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Override
    @Transactional(readOnly = true)
    public CVRReport generateCVR(Long contractId, LocalDate valuationDate) {
        log.info("Generating CVR for contract {} at valuation date {}", contractId, valuationDate);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        // Fetch latest approved application up to valuation date
        List<ApplicationForPayment> applications = applicationRepository
            .findByContractIdOrderByNumberDesc(contractId);

        ApplicationForPayment currentApp = applications.stream()
            .filter(a -> !a.getApplicationPeriodEnd().isAfter(valuationDate))
            .findFirst()
            .orElse(null);

        ApplicationForPayment previousApp = applications.size() > 1
            ? applications.get(1)
            : null;

        // Get BCIS indices for indexation
        BigDecimal[] indices = resolveBCISIndices(contract.getStartDate(), valuationDate);

        CVRReport report = CVRReport.builder()
            .contractId(contractId)
            .contractRef(contract.getContractRef())
            .contractTitle(contract.getTitle())
            .valuationDate(valuationDate)
            .applicationNumber(currentApp != null ? currentApp.getApplicationNumber() : 0)
            .measurementStandard(contract.getMeasurementStandard() != null
                ? contract.getMeasurementStandard().name() : null)
            .contractForm(contract.getContractForm() != null
                ? contract.getContractForm().name() : null)
            .bcisBaseIndex(indices[0])
            .bcisCurrentIndex(indices[1])
            .bcisAdjustmentFactor(indices[2])
            .contractSum(contract.getContractValue() != null
                ? contract.getContractValue() : BigDecimal.ZERO)
            .retentionPercent(contract.getRetentionPercent() != null
                ? contract.getRetentionPercent() : new BigDecimal("5.0"))
            .build();

        // === VALUE CALCULATIONS ===
        BigDecimal grossValue = calculateGrossValue(contractId, valuationDate);
        BigDecimal retentionHeld = calculateRetentionHeld(contractId);
        BigDecimal netValue = grossValue.subtract(retentionHeld);

        report.setValueMeasuredWork(currentApp != null && currentApp.getValueOfWorks() != null
            ? currentApp.getValueOfWorks() : BigDecimal.ZERO);
        report.setGrossValueToDate(grossValue);
        report.setLessRetentionToDate(retentionHeld);
        report.setNetValueToDate(netValue);

        // === COST CALCULATIONS ===
        BigDecimal[] costs = calculateCostsByCategory(contractId, valuationDate);
        BigDecimal totalCostPreIndex = costs[0];
        BigDecimal plantCost = costs[1];
        BigDecimal materialsCost = costs[2];
        BigDecimal labourCost = costs[3];
        BigDecimal subcontractCost = costs[4];
        BigDecimal dayworksCost = costs[5];
        BigDecimal disallowedCost = costs[6];

        report.setCostPlant(plantCost);
        report.setCostMaterials(materialsCost);
        report.setCostLabour(labourCost);
        report.setCostSubcontract(subcontractCost);
        report.setCostDayworks(dayworksCost);
        report.setCostDisallowed(disallowedCost);
        report.setCostTotalPreIndexation(totalCostPreIndex);
        report.setHasDisallowedCosts(disallowedCost.compareTo(BigDecimal.ZERO) > 0);

        // === BCIS INDEXATION ===
        // Apply indexation to materials costs only (per CESMM4 Schedule R)
        // Dayworks and labour are NOT index-linked
        BigDecimal indexFactor = indices[2];  // current/base
        BigDecimal indexedMaterialsCost = materialsCost.multiply(indexFactor)
            .setScale(2, RoundingMode.HALF_UP);

        report.setIndexedCostMaterials(indexedMaterialsCost);

        // Indexed total = plant (indexed) + materials (indexed) + labour + subcontract + dayworks
        // Note: labour (Series 4) and plant (Series 5) would use their respective indices
        // For simplicity, apply same factor to plant; production system would use separate series
        BigDecimal indexedPlantCost = plantCost.multiply(indexFactor)
            .setScale(2, RoundingMode.HALF_UP);
        report.setIndexedCostPlant(indexedPlantCost);

        BigDecimal indexedTotal = indexedMaterialsCost
            .add(indexedPlantCost)
            .add(labourCost)
            .add(subcontractCost)
            .add(dayworksCost);
        report.setIndexedCostTotal(indexedTotal);

        // === GROSS MARGIN ===
        BigDecimal grossMargin = grossValue.subtract(indexedTotal);
        BigDecimal marginPercent = BigDecimal.ZERO;
        if (grossValue.compareTo(BigDecimal.ZERO) > 0) {
            marginPercent = grossMargin.multiply(ONE_HUNDRED)
                .divide(grossValue, 2, RoundingMode.HALF_UP);
        }
        report.setGrossMargin(grossMargin);
        report.setGrossMarginPercent(marginPercent);

        // === RETENTION DETAIL ===
        populateRetentionDetail(contractId, report);

        // === EARTHWORKS BALANCE ===
        populateEarthworksBalance(contractId, report);

        // === EARLY WARNINGS ===
        // Avoid calling getEarlyWarnings() here because that method builds a CVR report
        // for margin/disallowed-cost checks. Calling it from generateCVR() creates a
        // generateCVR -> getEarlyWarnings -> generateCVR recursion and eventually a
        // StackOverflowError during tests and runtime report generation.
        BigDecimal earlyWarningAmount = BigDecimal.ZERO;
        BigDecimal contractWarningBase = contract.getContractValue() != null
            ? contract.getContractValue() : BigDecimal.ZERO;
        BigDecimal warningThreshold = contractWarningBase.multiply(new BigDecimal("1.05"));
        if (contractWarningBase.compareTo(BigDecimal.ZERO) > 0
            && indexedTotal.compareTo(warningThreshold) > 0) {
            earlyWarningAmount = indexedTotal.subtract(contractWarningBase);
        }
        report.setEarlyWarningAmount(earlyWarningAmount);
        report.setHasDisallowedCosts(disallowedCost.compareTo(BigDecimal.ZERO) > 0);

        // === PROGRESS ===
        BigDecimal progress = BigDecimal.ZERO;
        BigDecimal contractVal = contract.getContractValue();
        if (contractVal != null && contractVal.compareTo(BigDecimal.ZERO) > 0) {
            progress = grossValue.multiply(ONE_HUNDRED).divide(contractVal, 2, RoundingMode.HALF_UP);
        }
        report.setProgressPercent(progress.min(new BigDecimal("100.00")));
        report.setOverOrUnderValuation(grossValue.subtract(
            contractVal != null ? contractVal : BigDecimal.ZERO));

        // === FORECAST ===
        report.setValueForecastFinal(calculateForecastFinal(contract, grossValue));

        // === PACKAGE LINES ===
        report.setPackageLines(generatePackageLines(contract, currentApp, previousApp, indexFactor));

        log.info("CVR generated for contract {}: value={}, cost={}, margin={}%",
            contractId, grossValue, indexedTotal, marginPercent);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public CVRReport generateCVRByApplicationNumber(Long contractId, Integer applicationNumber) {
        ApplicationForPayment app = applicationRepository.findByContractId(contractId).stream()
            .filter(a -> Objects.equals(a.getApplicationNumber(), applicationNumber))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Application number " + applicationNumber + " not found for contract " + contractId));

        return generateCVR(contractId, app.getApplicationPeriodEnd());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CVRItem> getCVRSummary(LocalDate asOfDate) {
        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.IN_PROGRESS);
        List<CVRItem> items = new ArrayList<>();

        for (Contract contract : activeContracts) {
            CVRReport report = generateCVR(contract.getId(), asOfDate);
            items.add(CVRItem.builder()
                .contractId(contract.getId())
                .contractName(contract.getTitle())
                .valueToDate(report.getGrossValueToDate())
                .costToDate(report.getIndexedCostTotal())
                .grossMargin(report.getGrossMargin())
                .marginPercent(report.getGrossMarginPercent())
                .build());
        }

        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, CVRReport> getCVRTrend(Long contractId, LocalDate from, LocalDate to) {
        Map<String, CVRReport> trend = new LinkedHashMap<>();

        LocalDate periodEnd = from;
        int periodIndex = 0;
        while (!periodEnd.isAfter(to)) {
            String periodKey = "Period " + (++periodIndex) + " (" + periodEnd + ")";
            try {
                trend.put(periodKey, generateCVR(contractId, periodEnd));
            } catch (Exception e) {
                log.warn("Could not generate CVR for period {}: {}", periodEnd, e.getMessage());
            }
            periodEnd = periodEnd.plusMonths(1);
        }

        return trend;
    }

    @Override
    public BigDecimal getBCISAdjustmentFactor(int series, LocalDate currentDate, LocalDate baseDate) {
        if (currentDate == null || baseDate == null) {
            return BigDecimal.ONE;
        }

        if (currentDate.getYear() == baseDate.getYear()
            && currentDate.getMonthValue() == baseDate.getMonthValue()) {
            return new BigDecimal("1.000000");
        }

        BigDecimal currentIndex = bcisIndexRepository
            .getIndexValue(series, currentDate.getYear(), currentDate.getMonthValue())
            .or(() -> bcisIndexRepository
                .getMostRecentOnOrBefore(series, currentDate.getYear(), currentDate.getMonthValue())
                .map(b -> b.getIndexValue() != null ? b.getIndexValue() : b.getMaterialsIndex()))
            .orElse(DEFAULT_CURRENT_INDEX);
        BigDecimal baseIndex = bcisIndexRepository
            .getIndexValue(series, baseDate.getYear(), baseDate.getMonthValue())
            .or(() -> bcisIndexRepository
                .getMostRecentOnOrBefore(series, baseDate.getYear(), baseDate.getMonthValue())
                .map(b -> b.getIndexValue() != null ? b.getIndexValue() : b.getMaterialsIndex()))
            .orElse(DEFAULT_BASE_INDEX);

        if (baseIndex.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;  // No indexation if base index is 0
        }
        return currentIndex.divide(baseIndex, 6, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateEarthworksBalance(Long contractId) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null || contract.getSite() == null) {
            return BigDecimal.ZERO;
        }
        Long siteId = contract.getSite().getId();
        LocalDate start = contract.getStartDate() != null ? contract.getStartDate() : LocalDate.of(2000, 1, 1);
        List<MuckawayTicket> tickets = muckawayTicketRepository.findBySiteAndDateRange(siteId, start, LocalDate.now());
        BigDecimal exported = tickets.stream()
            .map(t -> t.getNetWeight() != null ? t.getNetWeight() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Positive = net import; negative = net export (muckaway-only contracts are typically negative)
        return BigDecimal.ZERO.subtract(exported);
    }

    @Override
    public Map<String, BigDecimal> getEarlyWarnings(Long contractId, LocalDate asOfDate) {
        Map<String, BigDecimal> warnings = new HashMap<>();

        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null) return warnings;

        // Check for over-valuation (value > contract sum)
        BigDecimal grossValue = calculateGrossValue(contractId, asOfDate);
        BigDecimal contractSum = contract.getContractValue() != null
            ? contract.getContractValue() : BigDecimal.ZERO;

        if (grossValue.compareTo(contractSum) > 0) {
            BigDecimal over = grossValue.subtract(contractSum);
            warnings.put("over_valuation", over);
        }

        // Check for negative margin (cost > value)
        CVRReport currentReport = generateCVR(contractId, asOfDate);
        if (currentReport.getGrossMargin().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal loss = currentReport.getGrossMargin().abs();
            warnings.put("negative_margin", loss);
        }

        // Check for disallowed costs
        if (currentReport.isHasDisallowedCosts()) {
            warnings.put("disallowed_costs", currentReport.getCostDisallowed());
        }

        // Early warning threshold: flag if forecast final cost > contract sum by >5%
        BigDecimal forecastCost = currentReport.getIndexedCostTotal();
        BigDecimal warningThreshold = contractSum.multiply(new BigDecimal("1.05"));
        if (forecastCost.compareTo(warningThreshold) > 0) {
            warnings.put("early_warning", forecastCost.subtract(contractSum));
        }

        return warnings;
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    /**
     * Resolve BCIS indices from database, with fallback defaults.
     * Returns: [baseIndex, currentIndex, adjustmentFactor]
     */
    private BigDecimal[] resolveBCISIndices(LocalDate contractStart, LocalDate valuationDate) {
        BigDecimal baseIndex = DEFAULT_BASE_INDEX;
        BigDecimal currentIndex = DEFAULT_CURRENT_INDEX;

        if (contractStart != null && bcisIndexRepository.count() > 0) {
            Optional<BigDecimal> dbBase = bcisIndexRepository.getIndexValue(
                BCIS_MATERIALS_SERIES, contractStart.getYear(), contractStart.getMonthValue());
            if (dbBase.isPresent()) baseIndex = dbBase.get();
        }

        if (valuationDate != null && bcisIndexRepository.count() > 0) {
            // Find most recent quarterly index on or before valuation date
            Optional<BigDecimal> dbCurrent = bcisIndexRepository.getIndexValue(
                BCIS_MATERIALS_SERIES, valuationDate.getYear(), valuationDate.getMonthValue());
            if (dbCurrent.isPresent()) currentIndex = dbCurrent.get();
        }

        BigDecimal adjustmentFactor = baseIndex.compareTo(BigDecimal.ZERO) > 0
            ? currentIndex.divide(baseIndex, 6, RoundingMode.HALF_UP)
            : BigDecimal.ONE;

        return new BigDecimal[]{baseIndex, currentIndex, adjustmentFactor};
    }

    /**
     * Find the BCIS index value for a given series and date.
     * Falls back to default if not in database.
     */
    private BigDecimal findIndexForDate(int series, LocalDate date) {
        if (date == null) return DEFAULT_CURRENT_INDEX;

        Optional<BigDecimal> idx = bcisIndexRepository.getIndexValue(series, date.getYear(), date.getMonthValue());
        if (idx.isPresent()) return idx.get();

        // Fallback: use nearest available quarter
        Optional<BigDecimal> nearest = bcisIndexRepository.getMostRecentOnOrBefore(series, date.getYear(), date.getMonthValue())
            .map(b -> b.getIndexValue() != null ? b.getIndexValue() : b.getMaterialsIndex());
        return nearest.orElse(DEFAULT_CURRENT_INDEX);
    }

    /**
     * Calculate the total gross value from approved applications up to the valuation date.
     * Includes: measured work + dayworks + variations from all approved applications.
     */
    private BigDecimal calculateGrossValue(Long contractId, LocalDate valuationDate) {
        List<ApplicationForPayment> applications = applicationRepository.findByContractId(contractId);
        return applications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.APPROVED
                || a.getStatus() == ApplicationStatus.CERTIFIED)
            .filter(a -> !a.getApplicationPeriodEnd().isAfter(valuationDate))
            .map(ApplicationForPayment::getGrossValue)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total retention held to date across all applications.
     */
    private BigDecimal calculateRetentionHeld(Long contractId) {
        List<ApplicationForPayment> applications = applicationRepository.findByContractId(contractId);
        return applications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.APPROVED
                || a.getStatus() == ApplicationStatus.CERTIFIED)
            .map(ApplicationForPayment::getRetention)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate costs by category for a contract up to the valuation date.
     * Returns array: [total, plant, materials, labour, subcontract, dayworks, disallowed]
     *
     * In a production system, these would query:
     * - Plant: HireRecordRepository (plant hire charges + fuel)
     * - Materials: MaterialDeliveryRepository (delivery records with costs)
     * - Labour: TimesheetRepository + OperativeRepository
     * - Subcontract: CISReturnRepository + ApplicationForPayment (subcontractor)
     * - Dayworks: direct daywork records
     * - Disallowed: flagged NEC4 cl.11.2(25) cost entries
     *
     * Currently uses ApplicationForPayment as proxy for all contract cost data.
     * Replace with proper cost sub-system queries.
     */
    /**
     * Estimate costs by category from approved application values.
     * Uses industry-typical groundworks cost ratios as a proxy until dedicated
     * cost sub-system entities (HireRecord, TimesheetEntry, etc.) are joined
     * to the contract. The ratios below (plant 25%, materials 35%, labour 20%,
     * subcontract 15%) are seeded from BCIS SMM averages for groundworks.
     * Returns: [total, plant, materials, labour, subcontract, dayworks, disallowed]
     */
    private BigDecimal[] calculateCostsByCategory(Long contractId, LocalDate valuationDate) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        Long siteId = (contract != null && contract.getSite() != null) ? contract.getSite().getId() : null;
        LocalDate contractStart = (contract != null && contract.getStartDate() != null)
                ? contract.getStartDate() : LocalDate.of(2000, 1, 1);

        // --- Labour: real wages from timesheets ---
        BigDecimal labour = BigDecimal.ZERO;
        if (siteId != null) {
            try {
                BigDecimal wages = timesheetRepository.calculateTotalWagesBySiteAndPeriod(
                        siteId, contractStart, valuationDate);
                if (wages != null) labour = wages;
            } catch (Exception e) {
                log.warn("Could not calculate labour costs for site {}: {}", siteId, e.getMessage());
            }
        }

        // --- Materials: sum of received purchase order net values ---
        BigDecimal materials = BigDecimal.ZERO;
        if (siteId != null) {
            try {
                BigDecimal poValue = purchaseOrderRepository.sumReceivedNetValueBySiteAndDateRange(
                        siteId, contractStart, valuationDate);
                if (poValue != null) materials = poValue;
            } catch (Exception e) {
                log.warn("Could not calculate materials costs for site {}: {}", siteId, e.getMessage());
            }
        }

        // --- Plant: daily hire rate × allocated days within contract period ---
        BigDecimal plant = BigDecimal.ZERO;
        if (siteId != null) {
            try {
                List<PlantAllocation> allocations = plantAllocationRepository
                        .findBySiteAndDateRangeWithPlant(siteId, contractStart, valuationDate);
                for (PlantAllocation alloc : allocations) {
                    if (alloc.getPlant() == null || alloc.getPlant().getDailyHireRate() == null) continue;
                    LocalDate allocStart = alloc.getStartDate() != null && alloc.getStartDate().isAfter(contractStart)
                            ? alloc.getStartDate() : contractStart;
                    LocalDate allocEnd = alloc.getEndDate() == null || alloc.getEndDate().isAfter(valuationDate)
                            ? valuationDate : alloc.getEndDate();
                    if (!allocStart.isAfter(allocEnd)) {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(allocStart, allocEnd) + 1;
                        plant = plant.add(alloc.getPlant().getDailyHireRate().multiply(BigDecimal.valueOf(days)));
                    }
                }
            } catch (Exception e) {
                log.warn("Could not calculate plant costs for site {}: {}", siteId, e.getMessage());
            }
        }

        // Subcontract: fallback to 15% of gross value (no subcontract AFP cost model yet)
        BigDecimal totalValue = calculateGrossValue(contractId, valuationDate);
        BigDecimal subcontract = totalValue.multiply(new BigDecimal("0.15"));

        // If all real values are zero (no data yet), fall back to full percentage model
        boolean hasRealData = labour.compareTo(BigDecimal.ZERO) > 0
                || materials.compareTo(BigDecimal.ZERO) > 0
                || plant.compareTo(BigDecimal.ZERO) > 0;
        if (!hasRealData) {
            plant = totalValue.multiply(new BigDecimal("0.25"));
            materials = totalValue.multiply(new BigDecimal("0.35"));
            labour = totalValue.multiply(new BigDecimal("0.20"));
        }

        BigDecimal total = plant.add(materials).add(labour).add(subcontract);
        return new BigDecimal[]{total, plant, materials, labour, subcontract, BigDecimal.ZERO, BigDecimal.ZERO};
    }

    /**
     * Populate retention ledger detail for the CVR report.
     */
    private void populateRetentionDetail(Long contractId, CVRReport report) {
        Optional<RetentionLedger> ledger = retentionLedgerRepository.findByContractId(contractId);
        if (ledger.isPresent()) {
            RetentionLedger rl = ledger.get();
            report.setRetentionHeld(rl.getTotalRetention() != null ? rl.getTotalRetention() : BigDecimal.ZERO);
            report.setRetentionReleasedPC(rl.getReleasedAtPC() != null ? rl.getReleasedAtPC() : BigDecimal.ZERO);
            report.setRetentionReleasedDefects(rl.getReleasedAtDefects() != null ? rl.getReleasedAtDefects() : BigDecimal.ZERO);
            report.setRetentionBalance(rl.getBalance() != null ? rl.getBalance() : BigDecimal.ZERO);
        } else {
            report.setRetentionHeld(BigDecimal.ZERO);
            report.setRetentionReleasedPC(BigDecimal.ZERO);
            report.setRetentionReleasedDefects(BigDecimal.ZERO);
            report.setRetentionBalance(BigDecimal.ZERO);
        }
    }

    /**
     * Populate earthworks (muckaway) detail for the CVR.
     * Exported spoil is summed from MuckawayTicket records for the contract site.
     * Imported fill uses the same records where netWeight represents inbound material.
     * Disposal cost (inc. landfill tax) is summed from disposalCost field.
     */
    private void populateEarthworksBalance(Long contractId, CVRReport report) {
        Contract contract = contractRepository.findById(contractId).orElse(null);
        if (contract == null || contract.getSite() == null) {
            report.setEarthworksImportedVolume(BigDecimal.ZERO);
            report.setEarthworksExportedVolume(BigDecimal.ZERO);
            report.setEarthworksBalance(BigDecimal.ZERO);
            report.setEarthworksImportedCost(BigDecimal.ZERO);
            report.setEarthworksExportedValue(BigDecimal.ZERO);
            return;
        }
        Long siteId = contract.getSite().getId();
        LocalDate start = contract.getStartDate() != null ? contract.getStartDate() : LocalDate.of(2000, 1, 1);
        LocalDate end = report.getValuationDate() != null ? report.getValuationDate() : LocalDate.now();

        List<MuckawayTicket> tickets = muckawayTicketRepository.findBySiteAndDateRange(siteId, start, end);

        BigDecimal exportedVolume = tickets.stream()
            .map(t -> t.getNetWeight() != null ? t.getNetWeight() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal exportedCost = tickets.stream()
            .map(t -> t.getDisposalCost() != null ? t.getDisposalCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.setEarthworksImportedVolume(BigDecimal.ZERO); // imported fill entity not yet available
        report.setEarthworksExportedVolume(exportedVolume);
        report.setEarthworksBalance(BigDecimal.ZERO.subtract(exportedVolume)); // net export is negative
        report.setEarthworksImportedCost(BigDecimal.ZERO);
        report.setEarthworksExportedValue(exportedCost);
    }

    /**
     * Calculate forecast final value based on current progress and estimate.
     */
    private BigDecimal calculateForecastFinal(Contract contract, BigDecimal grossValueToDate) {
        if (contract.getContractValue() == null) return grossValueToDate;

        BigDecimal progress = BigDecimal.ZERO;
        if (contract.getContractValue().compareTo(BigDecimal.ZERO) > 0) {
            progress = grossValueToDate.multiply(ONE_HUNDRED)
                .divide(contract.getContractValue(), 2, RoundingMode.HALF_UP);
        }

        // Simple linear forecast: if X% done at current date, forecast = current / (X/100)
        if (progress.compareTo(BigDecimal.ZERO) > 0) {
            return grossValueToDate.multiply(ONE_HUNDRED).divide(progress, 2, RoundingMode.HALF_UP);
        }

        return contract.getContractValue();
    }

    /**
     * Generate package-level line items for the CVR.
     * Currently creates a summary package; would be expanded with BoQ package data.
     */
    private List<CVRReport.CVRPackageLine> generatePackageLines(
            Contract contract,
            ApplicationForPayment currentApp,
            ApplicationForPayment previousApp,
            BigDecimal indexFactor) {

        List<CVRReport.CVRPackageLine> lines = new ArrayList<>();

        // Create a summary line for the entire contract
        // Production system: iterate over BoQ packages from tender/contract
        if (currentApp != null) {
            BigDecimal currentValue = currentApp.getValueOfWorks() != null
                ? currentApp.getValueOfWorks() : BigDecimal.ZERO;
            BigDecimal previousValue = previousApp != null && previousApp.getValueOfWorks() != null
                ? previousApp.getValueOfWorks() : BigDecimal.ZERO;
            BigDecimal thisPeriod = currentValue.subtract(previousValue);

            lines.add(CVRReport.CVRPackageLine.builder()
                .packageRef("CONTRACT")
                .description(contract.getTitle())
                .currentQuantity(thisPeriod)
                .previousQuantity(previousValue)
                .thisPeriodQuantity(thisPeriod)
                .rate(BigDecimal.ONE)
                .measuredValue(thisPeriod)
                .costToDate(thisPeriod)
                .indexedCostToDate(thisPeriod.multiply(indexFactor).divide(DEFAULT_BASE_INDEX, 2, RoundingMode.HALF_UP))
                .margin(currentValue.subtract(thisPeriod))
                .isDayworks(false)
                .isDisallowedCost(false)
                .build());
        }

        return lines;
    }
}
