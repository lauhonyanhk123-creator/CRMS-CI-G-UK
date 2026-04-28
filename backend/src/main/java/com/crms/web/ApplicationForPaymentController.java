package com.crms.web;

import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.request.PayLessNoticeRequest;
import com.crms.dto.request.PaymentNoticeRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.ApplicationResponse;
import com.crms.service.ApplicationForPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications for Payment", description = "Application for payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationForPaymentController {
    
    private final ApplicationForPaymentService applicationService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get application", description = "Get application for payment by ID")
    public ResponseEntity<ApiResponse<ApplicationResponse>> findById(@PathVariable Long id) {
        ApplicationResponse response = applicationService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Create application", description = "Create new application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(
            @RequestParam Long contractId,
            @Valid @RequestBody ApplicationForPaymentRequest request) {
        ApplicationResponse response = applicationService.create(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("Application created successfully", response));
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit application", description = "Submit application for payment")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submit(@PathVariable Long id) {
        ApplicationResponse response = applicationService.submit(id);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", response));
    }
    
    @PostMapping("/{id}/payment-notice")
    @Operation(summary = "Add payment notice", description = "Add payment notice to application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addPaymentNotice(
            @PathVariable Long id,
            @Valid @RequestBody PaymentNoticeRequest request) {
        ApplicationResponse response = applicationService.addPaymentNotice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment notice added", response));
    }
    
    @PostMapping("/{id}/pay-less-notice")
    @Operation(summary = "Add pay less notice", description = "Add pay less notice to application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addPayLessNotice(
            @PathVariable Long id,
            @Valid @RequestBody PayLessNoticeRequest request) {
        ApplicationResponse response = applicationService.addPayLessNotice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Pay less notice added", response));
    }
    
    @PostMapping("/{id}/default-payment-notice")
    @Operation(summary = "Add default payment notice", description = "Generate and add default payment notice")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addDefaultNotice(@PathVariable Long id) {
        ApplicationResponse response = applicationService.addDefaultNotice(id);
        return ResponseEntity.ok(ApiResponse.success("Default payment notice added", response));
    }
}