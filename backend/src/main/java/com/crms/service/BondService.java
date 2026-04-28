package com.crms.service;

import com.crms.dto.request.BondRequest;
import com.crms.dto.response.BondResponse;
import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface BondService {
    
    PageResponse<BondResponse> findAll(Map<String, Object> params);
    
    BondResponse findById(Long id);
    
    BondResponse findByBondNumber(String bondNumber);
    
    BondResponse create(Long adoptionCaseId, BondRequest request);
    
    BondResponse update(Long id, BondRequest request);
    
    BondResponse releaseBond(Long id, String releaseConditions);
    
    BondResponse partialRelease(Long id, java.math.BigDecimal releaseAmount);
    
    BondResponse markAsCalled(Long id, String reason);
    
    PageResponse<BondResponse> findExpiringBonds(int days);
    
    PageResponse<BondResponse> findExpiredBonds();
}
