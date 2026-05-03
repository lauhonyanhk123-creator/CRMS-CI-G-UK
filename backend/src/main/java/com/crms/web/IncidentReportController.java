package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.IncidentReportRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.IncidentReportResponse;
import com.crms.service.IncidentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incident-reports")
@RequiredArgsConstructor
@Tag(name = "Incident Reports", description = "Incident report management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class IncidentReportController {

    private final IncidentReportService incidentReportService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List incidents", description = "Get paginated list of incident reports")
    public ResponseEntity<ApiResponse<PageResponse<IncidentReportResponse>>> findAll(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("siteId", siteId);
        params.put("status", status);

        PageResponse<IncidentReportResponse> response = incidentReportService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create incident report", description = "Create a new incident report")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> create(
            @Valid @RequestBody IncidentReportRequest request) {
        IncidentReportResponse response = incidentReportService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Incident report created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get incident report", description = "Get incident report by ID")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> findById(@PathVariable Long id) {
        IncidentReportResponse response = incidentReportService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update incident report", description = "Update incident report details")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody IncidentReportRequest request) {
        IncidentReportResponse response = incidentReportService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incident report updated successfully", response));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit incident", description = "Submit incident report")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> submit(@PathVariable Long id) {
        IncidentReportResponse response = incidentReportService.submit(id);
        return ResponseEntity.ok(ApiResponse.success("Incident submitted", response));
    }

    @PostMapping("/{id}/investigate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Start investigation", description = "Start investigating incident")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> investigate(
            @PathVariable Long id,
            @RequestParam String outcome) {
        IncidentReportResponse response = incidentReportService.investigate(id, outcome);
        return ResponseEntity.ok(ApiResponse.success("Investigation started", response));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close incident", description = "Close incident report")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> close(@PathVariable Long id) {
        IncidentReportResponse response = incidentReportService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Incident closed", response));
    }

    @PostMapping("/{id}/riddor")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit RIDDOR", description = "Submit RIDDOR report to HSE")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> submitRIDDOR(
            @PathVariable Long id,
            @RequestParam(required = false) String hseRef) {
        IncidentReportResponse response = incidentReportService.submitRIDDOR(id, hseRef);
        return ResponseEntity.ok(ApiResponse.success("RIDDOR submitted", response));
    }

    @PostMapping("/{id}/mor")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit MOR", description = "Submit Method of Record")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> submitMOR(
            @PathVariable Long id,
            @RequestParam String conditions,
            @RequestParam String restrictions) {
        IncidentReportResponse response = incidentReportService.submitMOR(id, conditions, restrictions);
        return ResponseEntity.ok(ApiResponse.success("MOR submitted", response));
    }

    @PostMapping("/{id}/mor/sign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sign MOR", description = "Sign Method of Record")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> signMOR(
            @PathVariable Long id,
            @RequestParam String signedBy) {
        IncidentReportResponse response = incidentReportService.signMOR(id, signedBy);
        return ResponseEntity.ok(ApiResponse.success("MOR signed", response));
    }

    @PostMapping("/{id}/mor/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify MOR", description = "Verify Method of Record")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> verifyMOR(
            @PathVariable Long id,
            @RequestParam String verifiedBy) {
        IncidentReportResponse response = incidentReportService.verifyMOR(id, verifiedBy);
        return ResponseEntity.ok(ApiResponse.success("MOR verified", response));
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "By site", description = "Get incidents by site")
    public ResponseEntity<ApiResponse<PageResponse<IncidentReportResponse>>> findBySite(@PathVariable Long siteId) {
        PageResponse<IncidentReportResponse> response = incidentReportService.findBySiteId(siteId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/riddor-reportable")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "RIDDOR reportable", description = "Get RIDDOR reportable open incidents")
    public ResponseEntity<ApiResponse<PageResponse<IncidentReportResponse>>> findRIDDORReportable() {
        PageResponse<IncidentReportResponse> response = incidentReportService.findRIDDORReportable();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
