package com.crms.service;

public interface CISService {
    
    Object generateReturn(String taxMonth);
    
    Object submitReturn(Long id);
    
    Object generatePaymentStatements(Long returnId);
}