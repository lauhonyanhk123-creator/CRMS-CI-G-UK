package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.request.ContractRequest;
import com.crms.dto.request.PayLessNoticeRequest;
import com.crms.dto.request.PaymentNoticeRequest;
import com.crms.dto.response.*;
import com.crms.service.AdoptionCaseService;
import com.crms.service.ApplicationForPaymentService;
import com.crms.service.ContractService;
import com.crms.service.VariationService;
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
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Contract management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ContractController {
    
    private final ContractService contractService;
    private final ApplicationForPaymentService applicationService;
    private final VariationService variationService;
    private final AdoptionCaseService adoptionCaseService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List contracts", description = "Get paginated list of contracts")
    public ResponseEntity<ApiResponse<PageResponse<ContractResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("clientId", clientId);
        params.put("siteId", siteId);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<ContractResponse> response = contractService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/site/{siteId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get contracts by site", description = "Get all contracts for a site")
    public ResponseEntity<ApiResponse<PageResponse<ContractResponse>>> getContractsBySite(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<ContractResponse> response = contractService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create contract", description = "Create a new contract")
    public ResponseEntity<ApiResponse<ContractResponse>> create(@Valid @RequestBody ContractRequest request) {
        ContractResponse response = contractService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Contract created successfully", response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get contract", description = "Get contract by ID")
    public ResponseEntity<ApiResponse<ContractResponse>> findById(@PathVariable Long id) {
        ContractResponse response = contractService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update contract", description = "Update contract details")
    public ResponseEntity<ApiResponse<ContractResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ContractRequest request) {
        ContractResponse response = contractService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Contract updated successfully", response));
    }
    
    @GetMapping("/{id}/retention-ledger")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get retention ledger", description = "Get retention ledger for contract")
    public ResponseEntity<ApiResponse<RetentionLedgerResponse>> getRetentionLedger(@PathVariable Long id) {
        RetentionLedgerResponse response = contractService.getRetentionLedger(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}/variations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List variations", description = "Get list of variations for contract")
    public ResponseEntity<ApiResponse<PageResponse<VariationResponse>>> getVariations(@PathVariable Long id) {
        PageResponse<VariationResponse> response = variationService.findByContract(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create application", description = "Create application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationForPaymentRequest request) {
        ApplicationResponse response = applicationService.create(id, request);
        return ResponseEntity.ok(ApiResponse.success("Application created successfully", response));
    }
    
    @GetMapping("/{id}/applications")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List applications", description = "Get applications for contract")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationResponse>>> getApplications(@PathVariable Long id) {
        PageResponse<ApplicationResponse> response = applicationService.findByContract(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}/adoption-cases")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List adoption cases", description = "Get adoption cases for contract")
    public ResponseEntity<ApiResponse<PageResponse<AdoptionCaseResponse>>> getAdoptionCases(@PathVariable Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", id);
        return ResponseEntity.ok(ApiResponse.success(adoptionCaseService.findAll(params)));
    }
}
