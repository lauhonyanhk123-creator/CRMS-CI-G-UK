package com.crms.service.impl;

import com.crms.service.CISService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CISServiceImpl implements CISService {
    
    @Override
    public Object generateReturn(String taxMonth) {
        log.info("Generating CIS return for {}", taxMonth);
        return null;
    }
    
    @Override
    public Object submitReturn(Long id) {
        log.info("Submitting CIS return {}", id);
        return null;
    }
    
    @Override
    public Object generatePaymentStatements(Long returnId) {
        log.info("Generating payment statements for return {}", returnId);
        return null;
    }
}