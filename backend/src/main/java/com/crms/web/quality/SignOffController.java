package com.crms.web.quality;

import com.crms.dto.request.quality.SignOffRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.SignOffResponse;
import com.crms.service.quality.SignOffService;
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
@RequestMapping("/api/v1/quality/signoffs")
@RequiredArgsConstructor
@Tag(name = "Quality - Sign-offs", description = "NHBC/LABC sign-off management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SignOffController {

    private final SignOffService service;

    @GetMapping
    @Operation(summary = "List sign-offs", description = "Get paginated list of sign-offs")
    public ResponseEntity<ApiResponse<PageResponse<SignOffResponse>>> findAll(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String buildingControlType,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", contractId);
        params.put("buildingControlType", buildingControlType);
        params.put("result", result);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<SignOffResponse> response = service.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sign-off", description = "Get sign-off by ID")
    public ResponseEntity<ApiResponse<SignOffResponse>> findById(@PathVariable Long id) {
        SignOffResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create sign-off", description = "Create a new sign-off record")
    public ResponseEntity<ApiResponse<SignOffResponse>> create(@Valid @RequestBody SignOffRequest request) {
        SignOffResponse response = service.create(request);
        return ResponseEntity.ok(ApiResponse.success("Sign-off created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update sign-off", description = "Update sign-off details")
    public ResponseEntity<ApiResponse<SignOffResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SignOffRequest request) {
        SignOffResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Sign-off updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sign-off", description = "Delete sign-off")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Sign-off deleted successfully", null));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve sign-off", description = "Approve a sign-off with signature")
    public ResponseEntity<ApiResponse<SignOffResponse>> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String signature) {
        SignOffResponse response = service.approve(id, signature);
        return ResponseEntity.ok(ApiResponse.success("Sign-off approved", response));
    }

    @PostMapping("/{id}/refuse")
    @Operation(summary = "Refuse sign-off", description = "Refuse a sign-off with conditions")
    public ResponseEntity<ApiResponse<SignOffResponse>> refuse(
            @PathVariable Long id,
            @RequestParam String conditions) {
        SignOffResponse response = service.refuse(id, conditions);
        return ResponseEntity.ok(ApiResponse.success("Sign-off refused", response));
    }
}
