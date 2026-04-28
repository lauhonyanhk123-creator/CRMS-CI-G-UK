package com.crms.web;

import com.crms.dto.request.SiteRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SiteResponse;
import com.crms.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sites")
@RequiredArgsConstructor
@Tag(name = "Sites", description = "Site management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SiteController {
    
    private final SiteService siteService;
    
    @GetMapping
    @Operation(summary = "List sites", description = "Get paginated list of sites")
    public ResponseEntity<ApiResponse<PageResponse<SiteResponse>>> findAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", clientId);
        params.put("status", status);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<SiteResponse> response = siteService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Create site", description = "Create a new site")
    public ResponseEntity<ApiResponse<SiteResponse>> create(@Valid @RequestBody SiteRequest request) {
        SiteResponse response = siteService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Site created successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get site", description = "Get site by ID")
    public ResponseEntity<ApiResponse<SiteResponse>> findById(@PathVariable Long id) {
        SiteResponse response = siteService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update site", description = "Update site details")
    public ResponseEntity<ApiResponse<SiteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request) {
        SiteResponse response = siteService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Site updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete site", description = "Delete a site")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        siteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Site deleted successfully", null));
    }
}