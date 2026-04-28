package com.crms.service;

import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface ProcurementService {
    
    PageResponse<?> findRequisitions(Map<String, Object> params);
    
    Object createRequisition(Object request);
    
    Object approveRequisition(Long id);
    
    Object createPO(Long requisitionId);
    
    PageResponse<?> getDeliveryNotes(Map<String, Object> params);
}