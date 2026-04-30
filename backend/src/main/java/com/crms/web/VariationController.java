package com.crms.web;

import com.crms.dto.request.VariationRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.VariationResponse;
import com.crms.service.VariationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variations")
@RequiredArgsConstructor
@Tag(name = "Variations", description = "Variation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VariationController {
    
    private final VariationService variationService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List variations", description = "Get variations by contract")
    public ResponseEntity<ApiResponse<PageResponse<VariationResponse>>> findByContract(
            @RequestParam Long contractId) {
        PageResponse<VariationResponse> response = variationService.findByContract(contractId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create variation", description = "Create new variation")
    public ResponseEntity<ApiResponse<VariationResponse>> create(
            @RequestParam Long contractId,
            @Valid @RequestBody VariationRequest request) {
        VariationResponse response = variationService.create(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("Variation created successfully", response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get variation", description = "Get variation by ID")
    public ResponseEntity<ApiResponse<VariationResponse>> findById(@PathVariable Long id) {
        VariationResponse response = variationService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update variation", description = "Update variation")
    public ResponseEntity<ApiResponse<VariationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody VariationRequest request) {
        VariationResponse response = variationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Variation updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete variation", description = "Delete a variation")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        variationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Variation deleted successfully", null));
    }
}