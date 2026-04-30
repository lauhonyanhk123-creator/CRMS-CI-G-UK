package com.crms.web.quality;

import com.crms.dto.request.quality.DefectRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.DefectResponse;
import com.crms.service.quality.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quality/defects")
@RequiredArgsConstructor
@Tag(name = "Quality - Defects", description = "Defect management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DefectController {

    private final DefectService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List defects", description = "Get paginated list of defects")
    public ResponseEntity<ApiResponse<PageResponse<DefectResponse>>> findAll(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", contractId);
        params.put("status", status);
        params.put("priority", priority);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<DefectResponse> response = service.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get defect", description = "Get defect by ID")
    public ResponseEntity<ApiResponse<DefectResponse>> findById(@PathVariable Long id) {
        DefectResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create defect", description = "Create a new defect")
    public ResponseEntity<ApiResponse<DefectResponse>> create(@Valid @RequestBody DefectRequest request) {
        DefectResponse response = service.create(request);
        return ResponseEntity.ok(ApiResponse.success("Defect created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update defect", description = "Update defect details")
    public ResponseEntity<ApiResponse<DefectResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DefectRequest request) {
        DefectResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Defect updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete defect", description = "Delete defect")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Defect deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update defect status", description = "Update defect status")
    public ResponseEntity<ApiResponse<DefectResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        DefectResponse response = service.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Defect status updated", response));
    }

    @PatchMapping("/{id}/operative")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign operative", description = "Assign operative to defect")
    public ResponseEntity<ApiResponse<DefectResponse>> assignOperative(
            @PathVariable Long id,
            @RequestParam String operative) {
        DefectResponse response = service.assignOperative(id, operative);
        return ResponseEntity.ok(ApiResponse.success("Operative assigned", response));
    }

    @PatchMapping("/{id}/contractor")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign contractor", description = "Assign contractor to defect")
    public ResponseEntity<ApiResponse<DefectResponse>> assignContractor(
            @PathVariable Long id,
            @RequestParam String contractor) {
        DefectResponse response = service.assignContractor(id, contractor);
        return ResponseEntity.ok(ApiResponse.success("Contractor assigned", response));
    }
}
