package com.crms.web.quality;

import com.crms.dto.request.quality.InspectionRecordRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.InspectionRecordResponse;
import com.crms.service.quality.InspectionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quality/inspections")
@RequiredArgsConstructor
@Tag(name = "Quality - Inspection Records", description = "Inspection record management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InspectionRecordController {

    private final InspectionRecordService service;

    @GetMapping
    @Operation(summary = "List inspection records", description = "Get paginated list of inspection records")
    public ResponseEntity<ApiResponse<PageResponse<InspectionRecordResponse>>> findAll(
            @RequestParam(required = false) Long scheduleItemId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String inspectorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("scheduleItemId", scheduleItemId);
        params.put("result", result);
        params.put("inspectorName", inspectorName);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<InspectionRecordResponse> response = service.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inspection record", description = "Get inspection record by ID")
    public ResponseEntity<ApiResponse<InspectionRecordResponse>> findById(@PathVariable Long id) {
        InspectionRecordResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create inspection record", description = "Create a new inspection record")
    public ResponseEntity<ApiResponse<InspectionRecordResponse>> create(@Valid @RequestBody InspectionRecordRequest request) {
        InspectionRecordResponse response = service.create(request);
        return ResponseEntity.ok(ApiResponse.success("Inspection record created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update inspection record", description = "Update inspection record details")
    public ResponseEntity<ApiResponse<InspectionRecordResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody InspectionRecordRequest request) {
        InspectionRecordResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Inspection record updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inspection record", description = "Delete inspection record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Inspection record deleted successfully", null));
    }
}
