package com.crms.web;

import com.crms.dto.request.PlantItemRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PlantGanttItem;
import com.crms.dto.response.PlantItemResponse;
import com.crms.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/plant-items")
@RequiredArgsConstructor
@Tag(name = "Plant", description = "Plant and equipment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PlantController {
    
    private final PlantService plantService;
    
    @GetMapping
    @Operation(summary = "List plant items", description = "Get paginated list of plant items")
    public ResponseEntity<ApiResponse<PageResponse<PlantItemResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("category", category);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<PlantItemResponse> response = plantService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Create plant item", description = "Create new plant item")
    public ResponseEntity<ApiResponse<PlantItemResponse>> create(@Valid @RequestBody PlantItemRequest request) {
        PlantItemResponse response = plantService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Plant item created successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get plant item", description = "Get plant item by ID")
    public ResponseEntity<ApiResponse<PlantItemResponse>> findById(@PathVariable Long id) {
        PlantItemResponse response = plantService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update plant item", description = "Update plant item details")
    public ResponseEntity<ApiResponse<PlantItemResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PlantItemRequest request) {
        PlantItemResponse response = plantService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Plant item updated successfully", response));
    }
    
    @PostMapping("/{id}/loler-examinations")
    @Operation(summary = "Add LOLER examination", description = "Record LOLER examination for plant item")
    public ResponseEntity<ApiResponse<PlantItemResponse>> addLOLER(
            @PathVariable Long id,
            @RequestBody Object request) {
        PlantItemResponse response = plantService.addLOLER(id, request);
        return ResponseEntity.ok(ApiResponse.success("LOLER examination recorded", response));
    }
    
    @PostMapping("/{id}/puwer-inspections")
    @Operation(summary = "Add PUWER inspection", description = "Record PUWER inspection for plant item")
    public ResponseEntity<ApiResponse<PlantItemResponse>> addPUWER(
            @PathVariable Long id,
            @RequestBody Object request) {
        PlantItemResponse response = plantService.addPUWER(id, request);
        return ResponseEntity.ok(ApiResponse.success("PUWER inspection recorded", response));
    }
    
    @PostMapping("/{id}/allocations")
    @Operation(summary = "Add allocation", description = "Allocate plant item to operative")
    public ResponseEntity<ApiResponse<PlantItemResponse>> addAllocation(
            @PathVariable Long id,
            @RequestBody Object request) {
        PlantItemResponse response = plantService.addAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Allocation added", response));
    }
    
    @GetMapping("/gantt")
    @Operation(summary = "Get plant Gantt", description = "Get plant allocation Gantt chart data")
    public ResponseEntity<ApiResponse<List<PlantGanttItem>>> getPlantGantt(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<PlantGanttItem> response = plantService.getPlantGantt(from, to);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete plant item", description = "Delete a plant item")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        plantService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Plant item deleted", null));
    }
}