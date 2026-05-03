package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.BondRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.BondResponse;
import com.crms.dto.response.PageResponse;
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
@RequestMapping("/api/v1/bonds")
@RequiredArgsConstructor
@Tag(name = "Bonds", description = "Bond management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BondController {
    
    private final BondService bondService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List bonds", description = "Get paginated list of bonds")
    public ResponseEntity<ApiResponse<PageResponse<BondResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("contractId", contractId);
        params.put("page", page);
        params.put("size", size);
        
        PageResponse<BondResponse> response = bondService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get bond", description = "Get bond by ID")
    public ResponseEntity<ApiResponse<BondResponse>> findById(@PathVariable Long id) {
        BondResponse response = bondService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/number/{bondNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get bond by number", description = "Get bond by bond number")
    public ResponseEntity<ApiResponse<BondResponse>> findByBondNumber(@PathVariable String bondNumber) {
        BondResponse response = bondService.findByBondNumber(bondNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create bond", description = "Create new bond for adoption case")
    public ResponseEntity<ApiResponse<BondResponse>> create(
            @RequestParam Long adoptionCaseId,
            @Valid @RequestBody BondRequest request) {
        BondResponse response = bondService.create(adoptionCaseId, request);
        return ResponseEntity.ok(ApiResponse.success("Bond created", response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update bond", description = "Update bond details")
    public ResponseEntity<ApiResponse<BondResponse>> update(
            @PathVariable Long id,
            @RequestBody BondRequest request) {
        BondResponse response = bondService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bond updated", response));
    }
    
    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Release bond", description = "Release bond")
    public ResponseEntity<ApiResponse<BondResponse>> releaseBond(
            @PathVariable Long id,
            @RequestParam(required = false) String releaseConditions) {
        BondResponse response = bondService.releaseBond(id, releaseConditions);
        return ResponseEntity.ok(ApiResponse.success("Bond released", response));
    }
    
    @PostMapping("/{id}/partial-release")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Partial release", description = "Partially release bond")
    public ResponseEntity<ApiResponse<BondResponse>> partialRelease(
            @PathVariable Long id,
            @RequestParam java.math.BigDecimal amount) {
        BondResponse response = bondService.partialRelease(id, amount);
        return ResponseEntity.ok(ApiResponse.success("Bond partially released", response));
    }
    
    @PostMapping("/{id}/called")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark as called", description = "Mark bond as called")
    public ResponseEntity<ApiResponse<BondResponse>> markAsCalled(
            @PathVariable Long id,
            @RequestParam String reason) {
        BondResponse response = bondService.markAsCalled(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Bond marked as called", response));
    }
    
    @GetMapping("/expiring")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Expiring bonds", description = "Get bonds expiring within specified days")
    public ResponseEntity<ApiResponse<PageResponse<BondResponse>>> findExpiringBonds(
            @RequestParam(defaultValue = "30") int days) {
        PageResponse<BondResponse> response = bondService.findExpiringBonds(days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/expired")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Expired bonds", description = "Get expired active bonds")
    public ResponseEntity<ApiResponse<PageResponse<BondResponse>>> findExpiredBonds() {
        PageResponse<BondResponse> response = bondService.findExpiredBonds();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
