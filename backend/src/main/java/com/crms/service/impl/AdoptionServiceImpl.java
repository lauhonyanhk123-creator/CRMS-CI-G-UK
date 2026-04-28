package com.crms.service.impl;

import com.crms.dto.response.PageResponse;
import com.crms.service.AdoptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {
    
    @Override
    public PageResponse<?> findAll(Map<String, Object> params) {
        log.info("Finding adoption cases");
        return null;
    }
    
    @Override
    public Object findById(Long id) {
        log.info("Finding adoption case {}", id);
        return null;
    }
    
    @Override
    public Object create(Long contractId, Object request) {
        log.info("Creating adoption case for contract {}", contractId);
        return null;
    }
    
    @Override
    public Object addStage(Long id, Object stage) {
        log.info("Adding stage to adoption case {}", id);
        return null;
    }
    
    @Override
    public Object requestBondRelease(Long id) {
        log.info("Requesting bond release for adoption case {}", id);
        return null;
    }
}