package com.crms.service;

import com.crms.dto.request.*;
import com.crms.dto.response.*;

public interface HealthSafetyService {
    
    F10NotificationResponse createF10(Long contractId, F10CreateRequest request);
    
    ConstructionPhasePlanResponse createCPP(Long contractId, CPPCreateRequest request);
    
    RAMSDocumentResponse createRAMS(Long contractId, RAMSCreateRequest request);
    
    RAMSSignOnResponse signRAMS(Long ramsId, Long operativeId, Long siteId);
    
    PermitToDigResponse createPermit(PermitToDigCreateRequest request);
    
    PermitToDigResponse approvePermit(Long id);
    
    IncidentReportResponse createIncident(IncidentCreateRequest request);
}
