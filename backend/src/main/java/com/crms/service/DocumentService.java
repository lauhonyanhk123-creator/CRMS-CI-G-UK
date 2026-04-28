package com.crms.service;

import java.util.List;

public interface DocumentService {
    
    Object upload(Object file, Object metadata);
    
    List<Object> getVersions(Long id);
    
    String getDownloadUrl(Long id);
    
    void delete(Long id);
}