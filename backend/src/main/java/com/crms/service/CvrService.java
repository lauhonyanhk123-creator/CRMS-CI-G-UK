package com.crms.service;

import com.crms.dto.response.CVRReport;
import com.crms.dto.response.CVRItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Cost-Value Reconciliation engine.
 * Computes the relationship between the value of work done (valued by the QS)
 * and the actual cost incurred, producing a gross margin analysis for a contract.
 *
 * BCIS indexation follows CESMM4 Schedule R — materials costs are adjusted
 * using the BCIS All-in Materials Index (Series 3) relative to the base index
 * at contract start date. Dayworks are NOT index-linked (per CESMM4 R2).
 *
 * Disallowed costs (NEC4 cl.11.2(25)) are flagged separately and excluded
 * from the margin calculation.
 */
public interface CvrService {

    /**
     * Generate a full CVR report for a specific contract and valuation period.
     *
     * @param contractId   the contract ID
     * @param valuationDate the as-of date for the valuation (determines BCIS index lookup)
     * @return the complete CVRReport with package-level breakdown
     */
    CVRReport generateCVR(Long contractId, LocalDate valuationDate);

    /**
     * Generate a CVR report for a specific application number.
     */
    CVRReport generateCVRByApplicationNumber(Long contractId, Integer applicationNumber);

    /**
     * Get a summary-level CVR for all active contracts.
     */
    List<CVRItem> getCVRSummary(LocalDate asOfDate);

    /**
     * Get CVR trend data across multiple periods for charting.
     * Returns a map of period -> CVRReport for time-series analysis.
     */
    Map<String, CVRReport> getCVRTrend(Long contractId, LocalDate from, LocalDate to);

    /**
     * Get the BCIS adjustment factor for a given date.
     * Formula: currentIndex / baseIndex
     */
    BigDecimal getBCISAdjustmentFactor(int series, LocalDate currentDate, LocalDate baseDate);

    /**
     * Calculate earthworks balance (muckaway reconciliation).
     * Compares imported material tickets against exported spoil measurements.
     */
    BigDecimal calculateEarthworksBalance(Long contractId);

    /**
     * Check for disallowed costs and early warnings.
     * Returns a map of warning type -> amount.
     */
    Map<String, java.math.BigDecimal> getEarlyWarnings(Long contractId, LocalDate asOfDate);
}

