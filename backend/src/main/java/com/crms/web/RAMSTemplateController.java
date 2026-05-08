package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.RAMSTemplateRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.RAMSTemplateResponse;
import com.crms.service.RAMSTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/rams-templates")
@RequiredArgsConstructor
@Tag(name = "RAMS Templates", description = "RAMS template management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RAMSTemplateController {

    private final RAMSTemplateService ramsTemplateService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List RAMS templates", description = "Get paginated list of RAMS templates")
    public ResponseEntity<ApiResponse<PageResponse<RAMSTemplateResponse>>> findAll(
            @RequestParam(required = false) @Size(max = 100) String trade,
            @RequestParam(required = false) @Size(max = 200) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        params.put("trade", trade);
        params.put("search", search);

        PageResponse<RAMSTemplateResponse> response = ramsTemplateService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create RAMS template", description = "Create a new RAMS template")
    public ResponseEntity<ApiResponse<RAMSTemplateResponse>> create(
            @Valid @RequestBody RAMSTemplateRequest request) {
        RAMSTemplateResponse response = ramsTemplateService.create(request);
        return ResponseEntity.ok(ApiResponse.success("RAMS template created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get RAMS template", description = "Get RAMS template by ID")
    public ResponseEntity<ApiResponse<RAMSTemplateResponse>> findById(@PathVariable Long id) {
        RAMSTemplateResponse response = ramsTemplateService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update RAMS template", description = "Update RAMS template details")
    public ResponseEntity<ApiResponse<RAMSTemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RAMSTemplateRequest request) {
        RAMSTemplateResponse response = ramsTemplateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("RAMS template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete RAMS template", description = "Soft delete RAMS template")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ramsTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("RAMS template deleted successfully", null));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active templates", description = "Get all active RAMS templates")
    public ResponseEntity<ApiResponse<List<RAMSTemplateResponse>>> findActive() {
        List<RAMSTemplateResponse> response = ramsTemplateService.findActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/trades")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List trades", description = "Get distinct trade types")
    public ResponseEntity<ApiResponse<List<String>>> findDistinctTrades() {
        List<String> trades = ramsTemplateService.findDistinctTrades();
        return ResponseEntity.ok(ApiResponse.success(trades));
    }

    @GetMapping("/by-trade/{trade}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "By trade", description = "Get RAMS templates by trade")
    public ResponseEntity<ApiResponse<List<RAMSTemplateResponse>>> findByTrade(@PathVariable String trade) {
        List<RAMSTemplateResponse> response = ramsTemplateService.findByTrade(trade);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Copy template", description = "Create a copy of existing template")
    public ResponseEntity<ApiResponse<RAMSTemplateResponse>> copyTemplate(
            @PathVariable Long id,
            @RequestParam String newTitle) {
        RAMSTemplateResponse response = ramsTemplateService.copyTemplate(id, newTitle);
        return ResponseEntity.ok(ApiResponse.success("RAMS template copied successfully", response));
    }
}
