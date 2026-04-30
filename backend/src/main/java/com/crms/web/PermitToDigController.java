package com.crms.web;

import com.crms.dto.request.PermitToDigRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PermitToDigResponse;
import com.crms.service.PermitToDigService;
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
@RequestMapping("/api/v1/permits-to-dig")
@RequiredArgsConstructor
@Tag(name = "Permits to Dig", description = "Permit to dig management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PermitToDigController {

    private final PermitToDigService permitToDigService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List permits", description = "Get paginated list of permits to dig")
    public ResponseEntity<ApiResponse<PageResponse<PermitToDigResponse>>> findAll(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("siteId", siteId);
        params.put("status", status);

        PageResponse<PermitToDigResponse> response = permitToDigService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create permit", description = "Create a new permit to dig")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> create(
            @Valid @RequestBody PermitToDigRequest request) {
        PermitToDigResponse response = permitToDigService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Permit to dig created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get permit", description = "Get permit by ID")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> findById(@PathVariable Long id) {
        PermitToDigResponse response = permitToDigService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update permit", description = "Update permit details")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PermitToDigRequest request) {
        PermitToDigResponse response = permitToDigService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permit updated successfully", response));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit for precheck", description = "Submit permit for precheck")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> submitForPrecheck(@PathVariable Long id) {
        PermitToDigResponse response = permitToDigService.submitForPrecheck(id);
        return ResponseEntity.ok(ApiResponse.success("Permit submitted for precheck", response));
    }

    @PostMapping("/{id}/precheck")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Precheck", description = "Approve precheck")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> precheck(@PathVariable Long id) {
        PermitToDigResponse response = permitToDigService.precheck(id);
        return ResponseEntity.ok(ApiResponse.success("Permit prechecked", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject precheck", description = "Reject precheck with reason")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> rejectPrecheck(
            @PathVariable Long id,
            @RequestParam String reason) {
        PermitToDigResponse response = permitToDigService.rejectPrecheck(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Permit precheck rejected", response));
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Issue permit", description = "Issue permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> issue(@PathVariable Long id) {
        PermitToDigResponse response = permitToDigService.issue(id);
        return ResponseEntity.ok(ApiResponse.success("Permit issued", response));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Start work", description = "Start work on permit")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> startWork(@PathVariable Long id) {
        PermitToDigResponse response = permitToDigService.startWork(id);
        return ResponseEntity.ok(ApiResponse.success("Work started", response));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete permit", description = "Mark permit as completed")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> complete(@PathVariable Long id) {
        PermitToDigResponse response = permitToDigService.complete(id);
        return ResponseEntity.ok(ApiResponse.success("Permit completed", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel permit", description = "Cancel permit with reason")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> cancel(
            @PathVariable Long id,
            @RequestParam String reason) {
        PermitToDigResponse response = permitToDigService.cancel(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Permit cancelled", response));
    }

    @PostMapping("/{id}/extend")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Extend permit", description = "Extend permit end date")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> extend(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newEndDate) {
        PermitToDigResponse response = permitToDigService.extend(id, newEndDate);
        return ResponseEntity.ok(ApiResponse.success("Permit extended", response));
    }

    @GetMapping("/site/{siteId}/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Active by site", description = "Get active permit for site")
    public ResponseEntity<ApiResponse<PermitToDigResponse>> findActiveBySite(@PathVariable Long siteId) {
        PermitToDigResponse response = permitToDigService.findActiveBySiteId(siteId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
