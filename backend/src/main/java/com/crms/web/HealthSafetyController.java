package com.crms.web;

import com.crms.dto.response.ApiResponse;
import com.crms.service.HealthSafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/healthsafety")
@RequiredArgsConstructor
@Tag(name = "Health & Safety", description = "Health and safety management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class HealthSafetyController {
    
    private final HealthSafetyService healthSafetyService;
    
    @PostMapping("/f10")
    @Operation(summary = "Create F10", description = "Create F10 notification for contract")
    public ResponseEntity<ApiResponse<Object>> createF10(
            @RequestParam Long contractId,
            @RequestBody Object request) {
        Object response = healthSafetyService.createF10(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("F10 notification created", response));
    }
    
    @PostMapping("/cpp")
    @Operation(summary = "Create CPP", description = "Create Construction Phase Plan for contract")
    public ResponseEntity<ApiResponse<Object>> createCPP(
            @RequestParam Long contractId,
            @RequestBody Object request) {
        Object response = healthSafetyService.createCPP(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("CPP created", response));
    }
    
    @PostMapping("/rams")
    @Operation(summary = "Create RAMS", description = "Create RAMS document for contract")
    public ResponseEntity<ApiResponse<Object>> createRAMS(
            @RequestParam Long contractId,
            @RequestBody Object request) {
        Object response = healthSafetyService.createRAMS(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("RAMS created", response));
    }
    
    @PostMapping("/rams/{id}/sign")
    @Operation(summary = "Sign RAMS", description = "Operative signs RAMS document")
    public ResponseEntity<ApiResponse<Object>> signRAMS(
            @PathVariable Long ramsId,
            @RequestParam Long operativeId,
            @RequestParam Long siteId) {
        Object response = healthSafetyService.signRAMS(ramsId, operativeId, siteId);
        return ResponseEntity.ok(ApiResponse.success("RAMS signed", response));
    }
    
    @PostMapping("/permits/dig")
    @Operation(summary = "Create dig permit", description = "Create excavation dig permit")
    public ResponseEntity<ApiResponse<Object>> createDigPermit(@RequestBody Object request) {
        Object response = healthSafetyService.createPermit(request);
        return ResponseEntity.ok(ApiResponse.success("Dig permit created", response));
    }
    
    @PostMapping("/permits/{id}/approve")
    @Operation(summary = "Approve permit", description = "Approve a permit")
    public ResponseEntity<ApiResponse<Object>> approvePermit(@PathVariable Long id) {
        Object response = healthSafetyService.approvePermit(id);
        return ResponseEntity.ok(ApiResponse.success("Permit approved", response));
    }
    
    @PostMapping("/incidents")
    @Operation(summary = "Create incident", description = "Report an incident")
    public ResponseEntity<ApiResponse<Object>> createIncident(@RequestBody Object request) {
        Object response = healthSafetyService.createIncident(request);
        return ResponseEntity.ok(ApiResponse.success("Incident reported", response));
    }
}