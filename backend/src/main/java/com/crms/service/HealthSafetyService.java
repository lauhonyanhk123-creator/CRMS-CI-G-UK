package com.crms.service;

import java.util.Map;

public interface HealthSafetyService {
    
    Object createF10(Long contractId, Object request);
    
    Object createCPP(Long contractId, Object request);
    
    Object createRAMS(Long contractId, Object request);
    
    Object signRAMS(Long ramsId, Long operativeId, Long siteId);
    
    Object createPermit(Object request);
    
    Object approvePermit(Long id);
    
    Object createIncident(Object request);
}