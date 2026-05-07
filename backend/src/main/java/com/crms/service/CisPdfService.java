package com.crms.service;

public interface CisPdfService {

    /**
     * Generates a CIS300 Payment & Deduction Statement PDF for the given return.
     *
     * @param returnId the CIS return ID
     * @return PDF bytes ready for streaming to the client
     * @throws com.crms.exception.ResourceNotFoundException if the return does not exist
     * @throws IllegalStateException if the return has no lines
     */
    byte[] generatePaymentDeductionStatement(Long returnId);
}
