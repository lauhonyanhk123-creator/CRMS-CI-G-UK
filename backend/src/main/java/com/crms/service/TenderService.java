package com.crms.service;

import com.crms.dto.request.TenderRequest;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.TenderResponse;

import java.util.Map;

public interface TenderService {
    
    PageResponse<TenderResponse> findAll(Map<String, Object> params);
    
    TenderResponse findById(Long id);
    
    TenderResponse create(TenderRequest request);
    
    TenderResponse update(Long id, TenderRequest request);
    
    void delete(Long id);
    
    ContractResponse win(Long id);
    
    void lose(Long id, String reason);
}