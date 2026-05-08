package com.crms.web;

import com.crms.dto.request.*;
import com.crms.dto.response.*;
import com.crms.service.HealthSafetyService;
import java.util.List;
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
    
    @GetMapping("/f10")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List F10 notifications")
    public ResponseEntity<ApiResponse<List<F10NotificationResponse>>> listF10() {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.listF10()));
    }

    @GetMapping("/f10/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get F10 notification")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> getF10(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.getF10(id)));
    }

    @PostMapping("/f10")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create F10", description = "Create F10 notification for contract")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> createF10(
            @RequestParam Long contractId,
            @Valid @RequestBody F10CreateRequest request) {
        F10NotificationResponse response = healthSafetyService.createF10(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("F10 notification created", response));
    }

    @PutMapping("/f10/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update F10 notification")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> updateF10(
            @PathVariable Long id, @Valid @RequestBody F10CreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.updateF10(id, request)));
    }

    @DeleteMapping("/f10/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete F10 notification")
    public ResponseEntity<ApiResponse<Void>> deleteF10(@PathVariable Long id) {
        healthSafetyService.deleteF10(id);
        return ResponseEntity.ok(ApiResponse.success("F10 notification deleted", null));
    }
    
    @GetMapping("/cpp")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List CPPs")
    public ResponseEntity<ApiResponse<List<ConstructionPhasePlanResponse>>> listCPP() {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.listCPP()));
    }

    @GetMapping("/cpp/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get CPP")
    public ResponseEntity<ApiResponse<ConstructionPhasePlanResponse>> getCPP(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.getCPP(id)));
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

    @PutMapping("/cpp/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update CPP")
    public ResponseEntity<ApiResponse<ConstructionPhasePlanResponse>> updateCPP(
            @PathVariable Long id, @Valid @RequestBody CPPCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.updateCPP(id, request)));
    }

    @DeleteMapping("/cpp/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete CPP")
    public ResponseEntity<ApiResponse<Void>> deleteCPP(@PathVariable Long id) {
        healthSafetyService.deleteCPP(id);
        return ResponseEntity.ok(ApiResponse.success("CPP deleted", null));
    }
    
    @GetMapping("/rams")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List RAMS documents")
    public ResponseEntity<ApiResponse<List<RAMSDocumentResponse>>> listRAMS() {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.listRAMS()));
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

    @PutMapping("/rams/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update RAMS document")
    public ResponseEntity<ApiResponse<RAMSDocumentResponse>> updateRAMS(
            @PathVariable Long id, @Valid @RequestBody RAMSCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.updateRAMS(id, request)));
    }

    @DeleteMapping("/rams/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete RAMS document")
    public ResponseEntity<ApiResponse<Void>> deleteRAMS(@PathVariable Long id) {
        healthSafetyService.deleteRAMS(id);
        return ResponseEntity.ok(ApiResponse.success("RAMS deleted", null));
    }
    
    @PostMapping("/rams/{id}/sign")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sign RAMS", description = "Operative signs RAMS document")
    public ResponseEntity<ApiResponse<RAMSSignOnResponse>> signRAMS(
            @PathVariable("id") Long ramsId,
            @RequestParam Long operativeId,
            @RequestParam Long siteId) {
        RAMSSignOnResponse response = healthSafetyService.signRAMS(ramsId, operativeId, siteId);
        return ResponseEntity.ok(ApiResponse.success("RAMS signed", response));
    }
    
    @GetMapping("/permits")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List permits")
    public ResponseEntity<ApiResponse<List<PermitToDigResponse>>> listPermits() {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.listPermits()));
    }

    @PostMapping("/permits/dig")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create dig permit", description = "Create excavation dig permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> createDigPermit(
            @Valid @RequestBody PermitToDigCreateRequest request) {
        PermitToDigResponse response = healthSafetyService.createPermit(request);
        return ResponseEntity.ok(ApiResponse.success("Dig permit created", response));
    }

    @PutMapping("/permits/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> updatePermit(
            @PathVariable Long id, @Valid @RequestBody PermitToDigCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.updatePermit(id, request)));
    }

    @DeleteMapping("/permits/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete permit")
    public ResponseEntity<ApiResponse<Void>> deletePermit(@PathVariable Long id) {
        healthSafetyService.deletePermit(id);
        return ResponseEntity.ok(ApiResponse.success("Permit deleted", null));
    }
    
    @PostMapping("/permits/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve permit", description = "Approve a permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> approvePermit(@PathVariable Long id) {
        PermitToDigResponse response = healthSafetyService.approvePermit(id);
        return ResponseEntity.ok(ApiResponse.success("Permit approved", response));
    }
    
    @GetMapping("/incidents")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List incidents")
    public ResponseEntity<ApiResponse<List<IncidentReportResponse>>> listIncidents() {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.listIncidents()));
    }

    @PostMapping("/incidents")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create incident", description = "Report an incident")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> createIncident(
            @Valid @RequestBody IncidentCreateRequest request) {
        IncidentReportResponse response = healthSafetyService.createIncident(request);
        return ResponseEntity.ok(ApiResponse.success("Incident reported", response));
    }

    @PutMapping("/incidents/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update incident")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> updateIncident(
            @PathVariable Long id, @Valid @RequestBody IncidentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(healthSafetyService.updateIncident(id, request)));
    }

    @DeleteMapping("/incidents/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete incident")
    public ResponseEntity<ApiResponse<Void>> deleteIncident(@PathVariable Long id) {
        healthSafetyService.deleteIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incident deleted", null));
    }
}
