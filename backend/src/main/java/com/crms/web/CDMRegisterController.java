package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.CDMRegisterRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.CDMRegisterResponse;
import com.crms.service.CDMRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cdm-register")
@RequiredArgsConstructor
@Tag(name = "CDM Register", description = "CDM register management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CDMRegisterController {

    private final CDMRegisterService cdmRegisterService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List CDM registers", description = "Get paginated list of CDM registers")
    public ResponseEntity<ApiResponse<PageResponse<CDMRegisterResponse>>> findAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Boolean isNotifiable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("clientId", clientId);
        params.put("isNotifiable", isNotifiable);

        PageResponse<CDMRegisterResponse> response = cdmRegisterService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create CDM register", description = "Create a new CDM register")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> create(
            @Valid @RequestBody CDMRegisterRequest request) {
        CDMRegisterResponse response = cdmRegisterService.create(request);
        return ResponseEntity.ok(ApiResponse.success("CDM register created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get CDM register", description = "Get CDM register by ID")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> findById(@PathVariable Long id) {
        CDMRegisterResponse response = cdmRegisterService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update CDM register", description = "Update CDM register details")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CDMRegisterRequest request) {
        CDMRegisterResponse response = cdmRegisterService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("CDM register updated successfully", response));
    }

    @PostMapping("/{id}/submit-hse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit to HSE", description = "Submit CDM notification to HSE")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> submitToHSE(@PathVariable Long id) {
        CDMRegisterResponse response = cdmRegisterService.submitToHSE(id);
        return ResponseEntity.ok(ApiResponse.success("Submitted to HSE", response));
    }

    @PostMapping("/{id}/health-safety-file/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create HSF", description = "Create Health and Safety File")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> createHealthSafetyFile(@PathVariable Long id) {
        CDMRegisterResponse response = cdmRegisterService.createHealthSafetyFile(id);
        return ResponseEntity.ok(ApiResponse.success("Health and Safety File created", response));
    }

    @PostMapping("/{id}/health-safety-file/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete HSF", description = "Complete Health and Safety File")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> completeHealthSafetyFile(@PathVariable Long id) {
        CDMRegisterResponse response = cdmRegisterService.completeHealthSafetyFile(id);
        return ResponseEntity.ok(ApiResponse.success("Health and Safety File completed", response));
    }

    @GetMapping("/by-number/{notificationNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "By notification number", description = "Find by notification number")
    public ResponseEntity<ApiResponse<CDMRegisterResponse>> findByNotificationNumber(
            @PathVariable String notificationNumber) {
        CDMRegisterResponse response = cdmRegisterService.findByNotificationNumber(notificationNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "By client", description = "Get active CDM registers for client")
    public ResponseEntity<ApiResponse<PageResponse<CDMRegisterResponse>>> findByClient(@PathVariable Long clientId) {
        PageResponse<CDMRegisterResponse> response = cdmRegisterService.findActiveByClientId(clientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expiring")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Expiring projects", description = "Find projects expiring before date")
    public ResponseEntity<ApiResponse<PageResponse<CDMRegisterResponse>>> findExpiring(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PageResponse<CDMRegisterResponse> response = cdmRegisterService.findExpiringProjects(date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending-hse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Pending HSE notification", description = "Find projects pending HSE notification")
    public ResponseEntity<ApiResponse<PageResponse<CDMRegisterResponse>>> findPendingHseNotification() {
        PageResponse<CDMRegisterResponse> response = cdmRegisterService.findPendingHseNotification();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
