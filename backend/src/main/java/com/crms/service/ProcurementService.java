package com.crms.service;

import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface ProcurementService {
    
    PageResponse<?> findRequisitions(Map<String, Object> params);
    
    Object createRequisition(Object request);
    
    Object approveRequisition(Long id);
    
    Object createPO(Long requisitionId);
    
    PageResponse<?> getDeliveryNotes(Map<String, Object> params);
    default PageResponse<?> findPurchaseOrders(Map<String, Object> params) {
        return PageResponse.<Object>builder()
                .content(java.util.Collections.emptyList())
                .page(0).size(0).totalElements(0L).totalPages(0)
                .build();
    }
}