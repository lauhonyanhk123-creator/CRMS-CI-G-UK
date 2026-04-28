package com.crms.web;

import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/adoption-cases")
@RequiredArgsConstructor
@Tag(name = "Adoption Cases", description = "Adoption case management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdoptionController {
    
    private final AdoptionService adoptionService;
    
    @GetMapping
    @Operation(summary = "List adoption cases", description = "Get paginated list of adoption cases")
    public ResponseEntity<ApiResponse<PageResponse<?>>> findAll(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("contractId", contractId);
        params.put("status", status);
        params.put("page", page);
        params.put("size", size);
        
        PageResponse<?> response = adoptionService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Create adoption case", description = "Create new adoption case for contract")
    public ResponseEntity<ApiResponse<Object>> create(
            @RequestParam Long contractId,
            @RequestBody Object request) {
        Object response = adoptionService.create(contractId, request);
        return ResponseEntity.ok(ApiResponse.success("Adoption case created", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get adoption case", description = "Get adoption case by ID")
    public ResponseEntity<ApiResponse<Object>> findById(@PathVariable Long id) {
        Object response = adoptionService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/stage")
    @Operation(summary = "Add stage", description = "Add stage to adoption case")
    public ResponseEntity<ApiResponse<Object>> addStage(
            @PathVariable Long id,
            @RequestBody Object stage) {
        Object response = adoptionService.addStage(id, stage);
        return ResponseEntity.ok(ApiResponse.success("Stage added", response));
    }
    
    @PostMapping("/{id}/bond/release-request")
    @Operation(summary = "Bond release request", description = "Request bond release for adoption case")
    public ResponseEntity<ApiResponse<Object>> requestBondRelease(@PathVariable Long id) {
        Object response = adoptionService.requestBondRelease(id);
        return ResponseEntity.ok(ApiResponse.success("Bond release requested", response));
    }
}