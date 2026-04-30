package com.crms.web;

import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.request.PayLessNoticeRequest;
import com.crms.dto.request.PaymentNoticeRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.ApplicationResponse;
import com.crms.dto.response.PageResponse;
import com.crms.service.ApplicationForPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications for Payment", description = "Application for payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ApplicationForPaymentController {
    
    private final ApplicationForPaymentService applicationService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List applications", description = "Get applications by contract ID")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationResponse>>> findByContract(
            @RequestParam Long contractId) {
        PageResponse<ApplicationResponse> response = applicationService.findByContract(contractId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get application", description = "Get application for payment by ID")
    public ResponseEntity<ApiResponse<ApplicationResponse>> findById(@PathVariable Long id) {
        ApplicationResponse response = applicationService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create application", description = "Create new application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(
            @RequestParam Long contractId,
            @Valid @RequestBody ApplicationForPaymentRequest request) {
        ApplicationResponse response = applicationService.create(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("Application created successfully", response));
    }
    
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit application", description = "Submit application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submit(@PathVariable Long id) {
        ApplicationResponse response = applicationService.submit(id);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", response));
    }
    
    @PostMapping("/{id}/payment-notice")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add payment notice", description = "Add payment notice to application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addPaymentNotice(
            @PathVariable Long id,
            @Valid @RequestBody PaymentNoticeRequest request) {
        ApplicationResponse response = applicationService.addPaymentNotice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment notice added", response));
    }
    
    @PostMapping("/{id}/pay-less-notice")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add pay less notice", description = "Add pay less notice to application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addPayLessNotice(
            @PathVariable Long id,
            @Valid @RequestBody PayLessNoticeRequest request) {
        ApplicationResponse response = applicationService.addPayLessNotice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Pay less notice added", response));
    }
    
    @PostMapping("/{id}/default-payment-notice")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add default payment notice", description = "Generate and add default payment notice")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addDefaultNotice(@PathVariable Long id) {
        ApplicationResponse response = applicationService.addDefaultNotice(id);
        return ResponseEntity.ok(ApiResponse.success("Default payment notice added", response));
    }
    
    @PostMapping("/{id}/measure")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark application as measured", description = "Move application to MEASURED status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> measure(@PathVariable Long id) {
        ApplicationResponse response = applicationService.measure(id);
        return ResponseEntity.ok(ApiResponse.success("Application marked as measured", response));
    }
    
    @PostMapping("/{id}/agree")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Agree application", description = "Move application to AGREED status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> agree(@PathVariable Long id) {
        ApplicationResponse response = applicationService.agree(id);
        return ResponseEntity.ok(ApiResponse.success("Application agreed", response));
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve application", description = "Approve application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> approve(@PathVariable Long id) {
        ApplicationResponse response = applicationService.approve(id);
        return ResponseEntity.ok(ApiResponse.success("Application approved", response));
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject application", description = "Reject application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reject(@PathVariable Long id) {
        ApplicationResponse response = applicationService.reject(id);
        return ResponseEntity.ok(ApiResponse.success("Application rejected", response));
    }
    
    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark application as paid", description = "Mark approved application as paid")
    public ResponseEntity<ApiResponse<ApplicationResponse>> markPaid(@PathVariable Long id) {
        ApplicationResponse response = applicationService.markPaid(id);
        return ResponseEntity.ok(ApiResponse.success("Application marked as paid", response));
    }
}