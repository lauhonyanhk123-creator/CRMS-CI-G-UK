package com.crms.integration.cscs;

import com.crms.integration.dto.CscsCardVerificationResponse;

import java.util.List;

/**
 * CSCS Smart Check API Service interface.
 * Provides methods for verifying CSCS cards and checking operative qualifications.
 */
public interface CscsSmartCheckService {

    /**
     * Verify a CSCS card via Smart Check API.
     *
     * @param cardNumber The CSCS card number
     * @return Card verification response
     */
    CscsCardVerificationResponse verifyCard(String cardNumber);

    /**
     * Check if a CSCS card is expired.
     *
     * @param cardNumber The CSCS card number
     * @return true if expired
     */
    boolean checkCardExpiry(String cardNumber);

    /**
     * Get detailed operative information from CSCS card.
     *
     * @param cardNumber The CSCS card number
     * @return Card details including operative info
     */
    CscsCardVerificationResponse getCardDetails(String cardNumber);

    /**
     * Check operative qualifications against CSCS requirements.
     *
     * @param operativeId The operative ID
     * @return List of qualifications with validity status
     */
    List<CscsCardVerificationResponse.QualificationDto> checkOperativeQualifications(String operativeId);

    /**
     * Check if demo mode is active.
     *
     * @return true if using mock data
     */
    boolean isDemoMode();
}
