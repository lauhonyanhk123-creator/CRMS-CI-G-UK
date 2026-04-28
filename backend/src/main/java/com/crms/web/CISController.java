package com.crms.web;

import com.crms.dto.response.ApiResponse;
import com.crms.service.CISService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cis-returns")
@RequiredArgsConstructor
@Tag(name = "CIS Returns", description = "Construction Industry Scheme returns management")
@SecurityRequirement(name = "bearerAuth")
public class CISController {
    
    private final CISService cisService;
    
    @GetMapping
    @Operation(summary = "List CIS returns", description = "Get CIS returns for a tax month")
    public ResponseEntity<ApiResponse<Object>> findByTaxMonth(
            @RequestParam(required = false) String taxMonth) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/generate/{taxMonth}")
    @Operation(summary = "Generate return", description = "Generate CIS return for tax month")
    public ResponseEntity<ApiResponse<Object>> generateReturn(@PathVariable String taxMonth) {
        Object response = cisService.generateReturn(taxMonth);
        return ResponseEntity.ok(ApiResponse.success("CIS return generated", response));
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit return", description = "Submit CIS return to HMRC")
    public ResponseEntity<ApiResponse<Object>> submitReturn(@PathVariable Long id) {
        Object response = cisService.submitReturn(id);
        return ResponseEntity.ok(ApiResponse.success("CIS return submitted", response));
    }
    
    @GetMapping("/{id}/payment-deduction-statements")
    @Operation(summary = "Payment statements", description = "Get payment deduction statements for return")
    public ResponseEntity<ApiResponse<Object>> getPaymentStatements(@PathVariable Long id) {
        Object response = cisService.generatePaymentStatements(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}