package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.CVRItem;
import com.crms.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/cvr")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "CVR Report", description = "Get Cost Value Reconciliation report")
    public ResponseEntity<ApiResponse<List<CVRItem>>> getCVR(
            @RequestParam Long contract,
            @RequestParam(required = false) String period) {
        List<CVRItem> response = reportService.getCVR(contract, period);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/cashflow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cashflow Report", description = "Get cashflow forecast report")
    public ResponseEntity<ApiResponse<Object>> getCashflow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Object response = reportService.getCashflow(from.toString(), to.toString());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/retention-schedule")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retention Schedule", description = "Get retention schedule report")
    public ResponseEntity<ApiResponse<Object>> getRetentionSchedule() {
        Object response = reportService.getRetention();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/cis-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "CIS Summary", description = "Get CIS monthly summary")
    public ResponseEntity<ApiResponse<Object>> getCISSummary(
            @RequestParam(required = false) String taxMonth) {
        Object response = reportService.getCISSummary(taxMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/plant-utilization")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Plant Utilization", description = "Get plant utilization report")
    public ResponseEntity<ApiResponse<Object>> getPlantUtilization(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Map<String, Object> params = new HashMap<>();
        params.put("from", from.toString());
        params.put("to", to.toString());
        Object response = reportService.getPlantUtilization(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/tender-pipeline")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tender Pipeline", description = "Get tender pipeline report")
    public ResponseEntity<ApiResponse<Object>> getTenderPipeline() {
        Object response = reportService.getTenderPipeline();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
