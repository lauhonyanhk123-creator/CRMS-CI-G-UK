package com.crms.service;

import com.crms.dto.request.VariationRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.VariationResponse;

import java.util.Map;

public interface VariationService {
    
    PageResponse<VariationResponse> findByContract(Long contractId);

    PageResponse<VariationResponse> findAll();
    
    VariationResponse findById(Long id);
    
    VariationResponse create(Long contractId, VariationRequest request);
    
    VariationResponse update(Long id, VariationRequest request);
    
    void delete(Long id);
}