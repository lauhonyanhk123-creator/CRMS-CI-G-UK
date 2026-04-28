package com.crms.service;

import com.crms.dto.request.*;
import com.crms.dto.response.*;

import java.util.Map;

public interface AdoptionCaseService {
    
    PageResponse<AdoptionCaseResponse> findAll(Map<String, Object> params);
    
    AdoptionCaseResponse findById(Long id);
    
    AdoptionCaseResponse findByCaseRef(String caseRef);
    
    AdoptionCaseResponse create(AdoptionCaseRequest request);
    
    AdoptionCaseResponse update(Long id, AdoptionCaseRequest request);
    
    AdoptionCaseResponse updateStatus(Long id, com.crms.domain.adoption.enums.AdoptionStatus status);
    
    void delete(Long id);
    
    // Stage operations
    AdoptionStageResponse addStage(Long adoptionCaseId, AdoptionStageRequest request);
    
    AdoptionStageResponse updateStage(Long stageId, AdoptionStageRequest request);
    
    AdoptionStageResponse completeStage(Long stageId);
    
    // Snagging operations
    SnaggingItemResponse addSnaggingItem(SnaggingItemRequest request);
    
    SnaggingItemResponse updateSnaggingItem(Long id, SnaggingItemRequest request);
    
    SnaggingItemResponse completeSnaggingItem(Long id);
    
    SnaggingItemResponse verifySnaggingItem(Long id, String verifiedBy);
    
    PageResponse<SnaggingItemResponse> getSnaggingItems(Long adoptionCaseId, int page, int size);
    
    // Bond operations
    BondResponse createBond(Long adoptionCaseId, BondRequest request);
    
    BondResponse releaseBond(Long adoptionCaseId);
    
    // Commuted sum operations
    CommutedSumMovementResponse addCommutedSumMovement(Long adoptionCaseId, CommutedSumMovementRequest request);
    
    AdoptionCaseResponse getWithDetails(Long id);
}
