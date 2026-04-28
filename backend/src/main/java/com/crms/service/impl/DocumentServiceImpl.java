package com.crms.service.impl;

import com.crms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    
    @Override
    public Object upload(Object file, Object metadata) {
        log.info("Uploading document");
        return null;
    }
    
    @Override
    public List<Object> getVersions(Long id) {
        log.info("Getting document versions for {}", id);
        return new ArrayList<>();
    }
    
    @Override
    public String getDownloadUrl(Long id) {
        log.info("Getting download URL for {}", id);
        return null;
    }
    
    @Override
    public void delete(Long id) {
        log.info("Deleting document {}", id);
    }
}