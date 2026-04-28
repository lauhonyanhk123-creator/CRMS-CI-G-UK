package com.crms.service;

import com.crms.dto.request.CompanyRequest;
import com.crms.dto.response.CISVerificationResponse;
import com.crms.dto.response.CompanyResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;

import java.util.Map;

public interface CompanyService {
    
    PageResponse<CompanyResponse> findAll(Map<String, Object> params);
    
    CompanyResponse findById(Long id);
    
    CompanyResponse create(CompanyRequest request);
    
    CompanyResponse update(Long id, CompanyRequest request);
    
    void delete(Long id);
    
    CompanyResponse refreshCompaniesHouse(Long id);
    
    CISVerificationResponse verifyCIS(Long id);
    
    SubbieGateStatus getSubbieGateStatus(Long id);
}