package com.crms.service;

import com.crms.dto.response.*;

import java.util.Map;

public interface AdoptionService {
    
    PageResponse<AdoptionCaseResponse> findAll(Map<String, Object> params);
    
    AdoptionCaseResponse findById(Long id);
    
    AdoptionCaseResponse create(Long contractId, Object request);
    
    AdoptionStageCreateResponse addStage(Long id, Object stage);
    
    BondReleaseResponse requestBondRelease(Long id);
}
