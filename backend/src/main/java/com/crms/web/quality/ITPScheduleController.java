package com.crms.web.quality;

import com.crms.dto.request.quality.ITPScheduleRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPScheduleResponse;
import com.crms.service.quality.ITPScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quality/schedules")
@RequiredArgsConstructor
@Tag(name = "Quality - ITP Schedules", description = "ITP Schedule management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ITPScheduleController {

    private final ITPScheduleService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List ITP schedules", description = "Get paginated list of ITP schedules")
    public ResponseEntity<ApiResponse<PageResponse<ITPScheduleResponse>>> findAll(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", contractId);
        params.put("status", status);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<ITPScheduleResponse> response = service.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get ITP schedule", description = "Get ITP schedule by ID")
    public ResponseEntity<ApiResponse<ITPScheduleResponse>> findById(@PathVariable Long id) {
        ITPScheduleResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create ITP schedule", description = "Create a new ITP schedule")
    public ResponseEntity<ApiResponse<ITPScheduleResponse>> create(@Valid @RequestBody ITPScheduleRequest request) {
        ITPScheduleResponse response = service.create(request);
        return ResponseEntity.ok(ApiResponse.success("ITP Schedule created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update ITP schedule", description = "Update ITP schedule details")
    public ResponseEntity<ApiResponse<ITPScheduleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ITPScheduleRequest request) {
        ITPScheduleResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("ITP Schedule updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete ITP schedule", description = "Delete ITP schedule")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ITP Schedule deleted successfully", null));
    }

    @PostMapping("/from-template")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create schedule from template", description = "Create a new schedule from an existing template")
    public ResponseEntity<ApiResponse<ITPScheduleResponse>> createFromTemplate(
            @RequestParam Long templateId,
            @RequestParam Long contractId) {
        ITPScheduleResponse response = service.createFromTemplate(templateId, contractId);
        return ResponseEntity.ok(ApiResponse.success("ITP Schedule created from template", response));
    }

    @PostMapping("/{scheduleId}/items/{itemId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete schedule item", description = "Mark a schedule item as complete")
    public ResponseEntity<ApiResponse<ITPScheduleResponse>> completeItem(
            @PathVariable Long scheduleId,
            @PathVariable Long itemId,
            @RequestParam String completedBy,
            @RequestParam(required = false) String result) {
        ITPScheduleResponse response = service.completeItem(scheduleId, itemId, completedBy, result);
        return ResponseEntity.ok(ApiResponse.success("Schedule item completed", response));
    }
}
