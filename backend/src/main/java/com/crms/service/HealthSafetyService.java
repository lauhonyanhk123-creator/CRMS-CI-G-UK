package com.crms.service;

import com.crms.dto.request.*;
import com.crms.dto.response.*;

import java.util.List;

public interface HealthSafetyService {

    // F10
    F10NotificationResponse createF10(Long contractId, F10CreateRequest request);
    List<F10NotificationResponse> listF10();
    F10NotificationResponse getF10(Long id);
    F10NotificationResponse updateF10(Long id, F10CreateRequest request);
    void deleteF10(Long id);

    // CPP
    ConstructionPhasePlanResponse createCPP(Long contractId, CPPCreateRequest request);
    List<ConstructionPhasePlanResponse> listCPP();
    ConstructionPhasePlanResponse getCPP(Long id);
    ConstructionPhasePlanResponse updateCPP(Long id, CPPCreateRequest request);
    void deleteCPP(Long id);

    // RAMS
    RAMSDocumentResponse createRAMS(Long contractId, RAMSCreateRequest request);
    List<RAMSDocumentResponse> listRAMS();
    RAMSSignOnResponse signRAMS(Long ramsId, Long operativeId, Long siteId);
    RAMSDocumentResponse updateRAMS(Long id, RAMSCreateRequest request);
    void deleteRAMS(Long id);

    // Permits
    PermitToDigResponse createPermit(PermitToDigCreateRequest request);
    List<PermitToDigResponse> listPermits();
    PermitToDigResponse approvePermit(Long id);
    PermitToDigResponse updatePermit(Long id, PermitToDigCreateRequest request);
    void deletePermit(Long id);

    // Incidents
    IncidentReportResponse createIncident(IncidentCreateRequest request);
    List<IncidentReportResponse> listIncidents();
    IncidentReportResponse updateIncident(Long id, IncidentCreateRequest request);
    void deleteIncident(Long id);
}
