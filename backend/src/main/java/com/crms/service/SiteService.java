package com.crms.service;

import com.crms.dto.request.SiteRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SiteResponse;

import java.util.Map;

public interface SiteService {
    
    PageResponse<SiteResponse> findAll(Map<String, Object> params);
    
    SiteResponse findById(Long id);
    
    SiteResponse create(SiteRequest request);
    
    SiteResponse update(Long id, SiteRequest request);
    
    void delete(Long id);
}