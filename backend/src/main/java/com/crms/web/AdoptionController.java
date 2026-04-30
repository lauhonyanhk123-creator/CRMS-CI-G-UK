package com.crms.web;

import com.crms.dto.request.AdoptionCaseRequest;
import com.crms.dto.request.AdoptionStageRequest;
import com.crms.dto.request.BondRequest;
import com.crms.dto.request.CommutedSumMovementRequest;
import com.crms.dto.request.SnaggingItemRequest;
import com.crms.dto.response.*;
import com.crms.service.AdoptionCaseService;
import com.crms.service.BondService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/adoption-cases")
@RequiredArgsConstructor
@Tag(name = "Adoption Cases", description = "Adoption case management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdoptionController {
    
    private final AdoptionCaseService adoptionCaseService;
    private final BondService bondService;
    
    @GetMapping
    @PreAuthorize("hasRole('VIEWER') or hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "List adoption cases", description = "Get paginated list of adoption cases")
    public ResponseEntity<ApiResponse<PageResponse<AdoptionCaseResponse>>> findAll(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String adoptionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", contractId);
        params.put("status", status);
        params.put("adoptionType", adoptionType);
        params.put("page", page);
        params.put("size", size);
        
        PageResponse<AdoptionCaseResponse> response = adoptionCaseService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VIEWER') or hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Get adoption case", description = "Get adoption case by ID")
    public ResponseEntity<ApiResponse<AdoptionCaseResponse>> findById(@PathVariable Long id) {
        AdoptionCaseResponse response = adoptionCaseService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('VIEWER') or hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Get adoption case with details", description = "Get adoption case with bond, stages and movements")
    public ResponseEntity<ApiResponse<AdoptionCaseResponse>> getWithDetails(@PathVariable Long id) {
        AdoptionCaseResponse response = adoptionCaseService.getWithDetails(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/ref/{caseRef}")
    @PreAuthorize("hasRole('VIEWER') or hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Get adoption case by reference", description = "Get adoption case by case reference")
    public ResponseEntity<ApiResponse<AdoptionCaseResponse>> findByCaseRef(@PathVariable String caseRef) {
        AdoptionCaseResponse response = adoptionCaseService.findByCaseRef(caseRef);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Create adoption case", description = "Create new adoption case")
    public ResponseEntity<ApiResponse<AdoptionCaseResponse>> create(
            @Valid @RequestBody AdoptionCaseRequest request) {
        AdoptionCaseResponse response = adoptionCaseService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Adoption case created", response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Update adoption case", description = "Update adoption case")
    public ResponseEntity<ApiResponse<AdoptionCaseResponse>> update(
            @PathVariable Long id,
            @RequestBody AdoptionCaseRequest request) {
        AdoptionCaseResponse response = adoptionCaseService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Adoption case updated", response));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Update adoption case status", description = "Update adoption case workflow status")
    public ResponseEntity<ApiResponse<AdoptionCaseResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam com.crms.domain.adoption.enums.AdoptionStatus status) {
        AdoptionCaseResponse response = adoptionCaseService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete adoption case", description = "Delete adoption case (only PRE_APP or APPLICATION status)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        adoptionCaseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Adoption case deleted", null));
    }
    
    // Stage endpoints
    @PostMapping("/{id}/stages")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Add stage", description = "Add stage to adoption case")
    public ResponseEntity<ApiResponse<AdoptionStageResponse>> addStage(
            @PathVariable Long id,
            @Valid @RequestBody AdoptionStageRequest request) {
        AdoptionStageResponse response = adoptionCaseService.addStage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Stage added", response));
    }
    
    @PutMapping("/stages/{stageId}")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Update stage", description = "Update adoption stage")
    public ResponseEntity<ApiResponse<AdoptionStageResponse>> updateStage(
            @PathVariable Long stageId,
            @RequestBody AdoptionStageRequest request) {
        AdoptionStageResponse response = adoptionCaseService.updateStage(stageId, request);
        return ResponseEntity.ok(ApiResponse.success("Stage updated", response));
    }
    
    @PostMapping("/stages/{stageId}/complete")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Complete stage", description = "Mark stage as completed")
    public ResponseEntity<ApiResponse<AdoptionStageResponse>> completeStage(@PathVariable Long stageId) {
        AdoptionStageResponse response = adoptionCaseService.completeStage(stageId);
        return ResponseEntity.ok(ApiResponse.success("Stage completed", response));
    }
    
    // Snagging endpoints
    @PostMapping("/snagging")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Add snagging item", description = "Add snagging item to adoption case")
    public ResponseEntity<ApiResponse<SnaggingItemResponse>> addSnaggingItem(
            @Valid @RequestBody SnaggingItemRequest request) {
        SnaggingItemResponse response = adoptionCaseService.addSnaggingItem(request);
        return ResponseEntity.ok(ApiResponse.success("Snagging item added", response));
    }
    
    @PutMapping("/snagging/{id}")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Update snagging item", description = "Update snagging item")
    public ResponseEntity<ApiResponse<SnaggingItemResponse>> updateSnaggingItem(
            @PathVariable Long id,
            @RequestBody SnaggingItemRequest request) {
        SnaggingItemResponse response = adoptionCaseService.updateSnaggingItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Snagging item updated", response));
    }
    
    @PostMapping("/snagging/{id}/complete")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Complete snagging item", description = "Mark snagging item as completed")
    public ResponseEntity<ApiResponse<SnaggingItemResponse>> completeSnaggingItem(@PathVariable Long id) {
        SnaggingItemResponse response = adoptionCaseService.completeSnaggingItem(id);
        return ResponseEntity.ok(ApiResponse.success("Snagging item completed", response));
    }
    
    @PostMapping("/snagging/{id}/verify")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Verify snagging item", description = "Verify snagging item completion")
    public ResponseEntity<ApiResponse<SnaggingItemResponse>> verifySnaggingItem(
            @PathVariable Long id,
            @RequestParam String verifiedBy) {
        SnaggingItemResponse response = adoptionCaseService.verifySnaggingItem(id, verifiedBy);
        return ResponseEntity.ok(ApiResponse.success("Snagging item verified", response));
    }
    
    @GetMapping("/{id}/snagging")
    @PreAuthorize("hasRole('VIEWER') or hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "List snagging items", description = "Get snagging items for adoption case")
    public ResponseEntity<ApiResponse<PageResponse<SnaggingItemResponse>>> getSnaggingItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<SnaggingItemResponse> response = adoptionCaseService.getSnaggingItems(id, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // Bond endpoints
    @PostMapping("/{id}/bond")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Create bond", description = "Create bond for adoption case")
    public ResponseEntity<ApiResponse<BondResponse>> createBond(
            @PathVariable Long id,
            @Valid @RequestBody BondRequest request) {
        BondResponse response = adoptionCaseService.createBond(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bond created", response));
    }
    
    @PostMapping("/{id}/bond/release")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Release bond", description = "Request bond release for adoption case")
    public ResponseEntity<ApiResponse<BondResponse>> releaseBond(@PathVariable Long id) {
        BondResponse response = adoptionCaseService.releaseBond(id);
        return ResponseEntity.ok(ApiResponse.success("Bond release requested", response));
    }
    
    // Commuted sum endpoints
    @PostMapping("/{id}/commuted-sum")
    @PreAuthorize("hasRole('CONTRACTS') or hasRole('ADMIN')")
    @Operation(summary = "Add commuted sum movement", description = "Add commuted sum movement")
    public ResponseEntity<ApiResponse<CommutedSumMovementResponse>> addCommutedSumMovement(
            @PathVariable Long id,
            @Valid @RequestBody CommutedSumMovementRequest request) {
        CommutedSumMovementResponse response = adoptionCaseService.addCommutedSumMovement(id, request);
        return ResponseEntity.ok(ApiResponse.success("Commuted sum movement added", response));
    }
}
