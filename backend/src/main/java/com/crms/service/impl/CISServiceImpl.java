package com.crms.service.impl;

import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.CISService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CISServiceImpl implements CISService {
    
    private final CISReturnRepository cisReturnRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Object findAll(String taxMonth, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<CISReturn> returnPage = cisReturnRepository.findAll(pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", returnPage.getContent());
        result.put("page", returnPage.getNumber());
        result.put("size", returnPage.getSize());
        result.put("totalElements", returnPage.getTotalElements());
        result.put("totalPages", returnPage.getTotalPages());
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object findById(Long id) {
        CISReturn cisReturn = cisReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CISReturn", id));
        return cisReturn;
    }
    
    @Override
    @Transactional
    public Object generateReturn(String taxMonth) {
        log.info("Generating CIS return for {}", taxMonth);
        return null;
    }
    
    @Override
    @Transactional
    public Object submitReturn(Long id) {
        log.info("Submitting CIS return {}", id);
        return null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object generatePaymentStatements(Long returnId) {
        log.info("Generating payment statements for return {}", returnId);
        return null;
    }
}