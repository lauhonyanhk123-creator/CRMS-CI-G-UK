package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.TenderRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.TenderResponse;
import com.crms.service.TenderService;
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
@RequestMapping("/api/v1/tenders")
@RequiredArgsConstructor
@Tag(name = "Tenders", description = "Tender management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TenderController {
    
    private final TenderService tenderService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List tenders", description = "Get paginated list of tenders")
    public ResponseEntity<ApiResponse<PageResponse<TenderResponse>>> findAll(
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
        
        PageResponse<TenderResponse> response = tenderService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tenders by client", description = "Get all tenders for a client")
    public ResponseEntity<ApiResponse<PageResponse<TenderResponse>>> getTendersByClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<TenderResponse> response = tenderService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/site/{siteId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tenders by site", description = "Get all tenders for a site")
    public ResponseEntity<ApiResponse<PageResponse<TenderResponse>>> getTendersBySite(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<TenderResponse> response = tenderService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create tender", description = "Create a new tender")
    public ResponseEntity<ApiResponse<TenderResponse>> create(@Valid @RequestBody TenderRequest request) {
        TenderResponse response = tenderService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Tender created successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete tender", description = "Delete a tender")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        tenderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Tender deleted", null));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tender", description = "Get tender by ID")
    public ResponseEntity<ApiResponse<TenderResponse>> findById(@PathVariable Long id) {
        TenderResponse response = tenderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update tender", description = "Update tender details")
    public ResponseEntity<ApiResponse<TenderResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TenderRequest request) {
        TenderResponse response = tenderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tender updated successfully", response));
    }
    
    @PostMapping("/{id}/win")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Win tender", description = "Mark tender as won and create contract")
    public ResponseEntity<ApiResponse<ContractResponse>> win(@PathVariable Long id) {
        ContractResponse response = tenderService.win(id);
        return ResponseEntity.ok(ApiResponse.success("Tender won - contract created", response));
    }
    
    @PostMapping("/{id}/lose")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lose tender", description = "Mark tender as lost")
    public ResponseEntity<ApiResponse<Void>> lose(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        tenderService.lose(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Tender marked as lost", null));
    }
}
