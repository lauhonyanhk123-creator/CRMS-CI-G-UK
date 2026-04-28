package com.crms.service;

import com.crms.dto.response.PageResponse;

import java.util.List;

public interface DocumentService {
    
    PageResponse<Object> findAll(Long entityId, String entityType, String type, int page, int size);
    
    Object findById(Long id);
    
    Object upload(Object file, Object metadata);
    
    List<Object> getVersions(Long id);
    
    String getDownloadUrl(Long id);
    
    void delete(Long id);
}