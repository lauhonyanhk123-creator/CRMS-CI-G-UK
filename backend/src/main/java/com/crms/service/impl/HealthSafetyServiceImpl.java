package com.crms.service.impl;

import com.crms.service.HealthSafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthSafetyServiceImpl implements HealthSafetyService {
    
    @Override
    public Object createF10(Long contractId, Object request) {
        log.info("Creating F10 notification for contract {}", contractId);
        return null;
    }
    
    @Override
    public Object createCPP(Long contractId, Object request) {
        log.info("Creating CPP for contract {}", contractId);
        return null;
    }
    
    @Override
    public Object createRAMS(Long contractId, Object request) {
        log.info("Creating RAMS for contract {}", contractId);
        return null;
    }
    
    @Override
    public Object signRAMS(Long ramsId, Long operativeId, Long siteId) {
        log.info("RAMS {} signed by operative {} at site {}", ramsId, operativeId, siteId);
        return null;
    }
    
    @Override
    public Object createPermit(Object request) {
        log.info("Creating permit");
        return null;
    }
    
    @Override
    public Object approvePermit(Long id) {
        log.info("Approving permit {}", id);
        return null;
    }
    
    @Override
    public Object createIncident(Object request) {
        log.info("Creating incident report");
        return null;
    }
}