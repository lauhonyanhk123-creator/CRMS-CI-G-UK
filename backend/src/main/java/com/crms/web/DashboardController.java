package com.crms.web;

import com.crms.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPI Dashboard & Analytics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dashboard stats", description = "Get key metrics for dashboard KPI cards")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Top-level KPI summary")
    public ResponseEntity<Map<String, Object>> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    @GetMapping("/activity-feed")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Recent activity feed", description = "Last N audit log entries for the activity stream")
    public ResponseEntity<List<Map<String, Object>>> getActivityFeed(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(dashboardService.getActivityFeed(limit));
    }

    @GetMapping("/expiring-items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Expiring items requiring action", description = "Items expiring within N days across all domains")
    public ResponseEntity<Map<String, Object>> getExpiringItems(
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(dashboardService.getExpiringItems(days));
    }

    @GetMapping("/pipeline-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tender pipeline funnel", description = "Win/loss funnel across tender statuses")
    public ResponseEntity<Map<String, Object>> getPipelineSummary() {
        return ResponseEntity.ok(dashboardService.getPipelineSummary());
    }

    @GetMapping("/contract-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Contract status summary")
    public ResponseEntity<Map<String, Object>> getContractSummary() {
        return ResponseEntity.ok(dashboardService.getContractSummary());
    }

    @GetMapping("/cashflow-forecast")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cash flow forecast", description = "Forecast of incoming payments from approved applications for payment")
    public ResponseEntity<Map<String, Object>> getCashflowForecast(
            @RequestParam(defaultValue = "12") int monthsAhead) {
        return ResponseEntity.ok(dashboardService.getCashflowForecast(monthsAhead));
    }

    @GetMapping("/retention-schedule")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retention schedule", description = "Retention held and upcoming release dates")
    public ResponseEntity<List<Map<String, Object>>> getRetentionSchedule() {
        return ResponseEntity.ok(dashboardService.getRetentionSchedule());
    }

    @GetMapping("/health-safety-stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "H&S statistics", description = "RIDDOR stats, AFR, near-miss ratio over N months")
    public ResponseEntity<Map<String, Object>> getHealthSafetyStats(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(dashboardService.getHealthSafetyStats(months));
    }

    @GetMapping("/plant-utilisation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Plant utilisation", description = "Plant allocation vs available over N days")
    public ResponseEntity<Map<String, Object>> getPlantUtilisation(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getPlantUtilisation(days));
    }

    @GetMapping("/cis-deductions-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "CIS deductions summary", description = "Monthly CIS deductions for the current tax year")
    public ResponseEntity<Map<String, Object>> getCisSummary() {
        return ResponseEntity.ok(dashboardService.getCisSummary());
    }

    @GetMapping("/adoption-status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adoption cases status")
    public ResponseEntity<Map<String, Object>> getAdoptionStatus() {
        return ResponseEntity.ok(dashboardService.getAdoptionStatus());
    }

    @GetMapping("/procurement-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Procurement overview")
    public ResponseEntity<Map<String, Object>> getProcurementSummary() {
        return ResponseEntity.ok(dashboardService.getProcurementSummary());
    }
}
