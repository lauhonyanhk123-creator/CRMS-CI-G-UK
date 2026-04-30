package com.crms.web;

import com.crms.dto.request.F10NotificationRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.F10NotificationResponse;
import com.crms.service.F10NotificationService;
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
@RequestMapping("/api/v1/f10-notifications")
@RequiredArgsConstructor
@Tag(name = "F10 Notifications", description = "F10 notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class F10NotificationController {

    private final F10NotificationService f10NotificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List F10 notifications", description = "Get paginated list of F10 notifications")
    public ResponseEntity<ApiResponse<PageResponse<F10NotificationResponse>>> findAll(
            @RequestParam(required = false) Long contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("contractId", contractId);

        PageResponse<F10NotificationResponse> response = f10NotificationService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create F10 notification", description = "Create a new F10 notification")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> create(
            @RequestParam Long contractId,
            @Valid @RequestBody F10NotificationRequest request) {
        F10NotificationResponse response = f10NotificationService.create(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("F10 notification created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get F10 notification", description = "Get F10 notification by ID")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> findById(@PathVariable Long id) {
        F10NotificationResponse response = f10NotificationService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update F10 notification", description = "Update F10 notification details")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody F10NotificationRequest request) {
        F10NotificationResponse response = f10NotificationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("F10 notification updated successfully", response));
    }

    @PostMapping("/{id}/submit-hse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit to HSE", description = "Submit notification to HSE")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> submitToHSE(@PathVariable Long id) {
        F10NotificationResponse response = f10NotificationService.submitToHSE(id);
        return ResponseEntity.ok(ApiResponse.success("Submitted to HSE", response));
    }

    @PostMapping("/{id}/acknowledge-hdf")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Acknowledge HDF", description = "Acknowledge HDF submission")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> acknowledgeHDF(@PathVariable Long id) {
        F10NotificationResponse response = f10NotificationService.acknowledgeHDF(id);
        return ResponseEntity.ok(ApiResponse.success("HDF acknowledged", response));
    }

    @GetMapping("/contract/{contractId}/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Active by contract", description = "Get active F10 for contract")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> findActiveByContract(@PathVariable Long contractId) {
        F10NotificationResponse response = f10NotificationService.findActiveByContractId(contractId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-number/{notificationNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "By notification number", description = "Find by notification number")
    public ResponseEntity<ApiResponse<F10NotificationResponse>> findByNotificationNumber(
            @PathVariable String notificationNumber) {
        F10NotificationResponse response = f10NotificationService.findByNotificationNumber(notificationNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expiring")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Expiring notifications", description = "Find notifications expiring before date")
    public ResponseEntity<ApiResponse<PageResponse<F10NotificationResponse>>> findExpiring(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PageResponse<F10NotificationResponse> response = f10NotificationService.findExpiringNotifications(date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
