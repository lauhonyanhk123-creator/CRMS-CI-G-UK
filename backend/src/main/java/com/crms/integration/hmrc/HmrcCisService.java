package com.crms.integration.hmrc;

import com.crms.integration.dto.*;
import com.crms.integration.dto.HmrcCisVerificationResponse.CisVerificationResult;

import java.math.BigDecimal;

/**
 * HMRC CIS (Construction Industry Scheme) Service interface.
 * Provides methods for verifying subcontractors, checking deduction rates,
 * submitting monthly CIS returns, and calculating deductions.
 */
public interface HmrcCisService {

    /**
     * Verify a subcontractor's CIS status using their UTR number.
     *
     * @param utr The Unique Taxpayer Reference number (10 digits)
     * @return Verification response with status and deduction rate
     */
    HmrcCisVerificationResponse verifySubcontractor(String utr);

    /**
     * Get the applicable CIS deduction percentage for a subcontractor/contractor pair.
     *
     * @param supplierUtr The subcontractor's UTR
     * @param contractorUtr The contractor's UTR
     * @return Deduction rate response
     */
    HmrcCisDeductionRateResponse getDeductionPercentage(String supplierUtr, String contractorUtr);

    /**
     * Submit a monthly CIS return to HMRC.
     *
     * @param cisReturn The CIS return data to submit
     * @return Submission response with receipt reference
     */
    HmrcCisSubmitResponse submitMonthlyReturn(CisReturnDto cisReturn);

    /**
     * Calculate CIS deduction amount based on gross payment and applicable rate.
     *
     * @param grossAmount The gross payment amount
     * @param cisRate The CIS deduction rate (e.g., 20 for 20%)
     * @return The calculated deduction amount
     */
    BigDecimal calculateDeduction(BigDecimal grossAmount, BigDecimal cisRate);

    /**
     * Check if the service is running in demo/mock mode.
     *
     * @return true if in demo mode
     */
    boolean isDemoMode();
}
