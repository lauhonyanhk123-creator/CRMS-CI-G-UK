package com.crms.web;

import com.crms.dto.request.*;
import com.crms.dto.response.*;
import com.crms.service.HealthSafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/healthsafety")
@RequiredArgsConstructor
@Tag(name = "Health & Safety", description = "Health and safety management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class HealthSafetyController {
    
    private final HealthSafetyService healthSafetyService;
    
    @PostMapping("/f10")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create F10", description = "Create F10 notification for contract")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> createF10(
            @RequestParam Long contractId,
            @Valid @RequestBody F10CreateRequest request) {
        F10NotificationResponse response = healthSafetyService.createF10(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("F10 notification created", response));
    }
    
    @PostMapping("/cpp")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create CPP", description = "Create Construction Phase Plan for contract")
    public ResponseEntity<ApiResponse<ConstructionPhasePlanResponse>> createCPP(
            @RequestParam Long contractId,
            @Valid @RequestBody CPPCreateRequest request) {
        ConstructionPhasePlanResponse response = healthSafetyService.createCPP(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("CPP created", response));
    }
    
    @PostMapping("/rams")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create RAMS", description = "Create RAMS document for contract")
    public ResponseEntity<ApiResponse<RAMSDocumentResponse>> createRAMS(
            @RequestParam Long contractId,
            @Valid @RequestBody RAMSCreateRequest request) {
        RAMSDocumentResponse response = healthSafetyService.createRAMS(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("RAMS created", response));
    }
    
    @PostMapping("/rams/{id}/sign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sign RAMS", description = "Operative signs RAMS document")
    public ResponseEntity<ApiResponse<RAMSSignOnResponse>> signRAMS(
            @PathVariable Long ramsId,
            @RequestParam Long operativeId,
            @RequestParam Long siteId) {
        RAMSSignOnResponse response = healthSafetyService.signRAMS(ramsId, operativeId, siteId);
        return ResponseEntity.ok(ApiResponse.success("RAMS signed", response));
    }
    
    @PostMapping("/permits/dig")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create dig permit", description = "Create excavation dig permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> createDigPermit(
            @Valid @RequestBody PermitToDigCreateRequest request) {
        PermitToDigResponse response = healthSafetyService.createPermit(request);
        return ResponseEntity.ok(ApiResponse.success("Dig permit created", response));
    }
    
    @PostMapping("/permits/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve permit", description = "Approve a permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> approvePermit(@PathVariable Long id) {
        PermitToDigResponse response = healthSafetyService.approvePermit(id);
        return ResponseEntity.ok(ApiResponse.success("Permit approved", response));
    }
    
    @PostMapping("/incidents")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create incident", description = "Report an incident")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> createIncident(
            @Valid @RequestBody IncidentCreateRequest request) {
        IncidentReportResponse response = healthSafetyService.createIncident(request);
        return ResponseEntity.ok(ApiResponse.success("Incident reported", response));
    }
}
