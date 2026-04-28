package com.crms.service;

import java.util.Map;

public interface AdoptionService {
    
    PageResponse<?> findAll(Map<String, Object> params);
    
    Object findById(Long id);
    
    Object create(Long contractId, Object request);
    
    Object addStage(Long id, Object stage);
    
    Object requestBondRelease(Long id);
}