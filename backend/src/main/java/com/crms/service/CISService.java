package com.crms.service;

public interface CISService {
    
    Object findAll(String taxMonth, int page, int size);
    
    Object findById(Long id);
    
    Object generateReturn(String taxMonth);
    
    Object submitReturn(Long id);
    
    Object generatePaymentStatements(Long returnId);
}