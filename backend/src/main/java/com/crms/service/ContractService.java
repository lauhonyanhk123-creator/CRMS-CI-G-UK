package com.crms.service;

import com.crms.dto.request.ContractRequest;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.RetentionLedgerResponse;

import java.util.Map;

public interface ContractService {
    
    PageResponse<ContractResponse> findAll(Map<String, Object> params);
    
    ContractResponse findById(Long id);
    
    ContractResponse create(ContractRequest request);
    
    ContractResponse update(Long id, ContractRequest request);
    
    RetentionLedgerResponse getRetentionLedger(Long id);
}