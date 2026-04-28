package com.crms.service;

import com.crms.dto.request.OperativeRequest;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;

import java.util.Map;

public interface OperativeService {
    
    PageResponse<OperativeResponse> findAll(Map<String, Object> params);
    
    OperativeResponse findById(Long id);
    
    OperativeResponse create(OperativeRequest request);
    
    OperativeResponse update(Long id, OperativeRequest request);
    
    SubbieGateStatus smartCheckCard(Long operativeId, Long cardId);
    
    SubbieGateStatus getSubbieGateStatus(Long operativeId);
    
    void delete(Long id);
}