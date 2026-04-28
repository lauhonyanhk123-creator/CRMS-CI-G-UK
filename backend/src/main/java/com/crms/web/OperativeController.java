package com.crms.web;

import com.crms.dto.request.OperativeRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.service.OperativeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/operatives")
@RequiredArgsConstructor
@Tag(name = "Operatives", description = "Operative management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OperativeController {
    
    private final OperativeService operativeService;
    
    @GetMapping
    @Operation(summary = "List operatives", description = "Get paginated list of operatives")
    public ResponseEntity<ApiResponse<PageResponse<OperativeResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<OperativeResponse> response = operativeService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Create operative", description = "Create new operative")
    public ResponseEntity<ApiResponse<OperativeResponse>> create(@Valid @RequestBody OperativeRequest request) {
        OperativeResponse response = operativeService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Operative created successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get operative", description = "Get operative by ID")
    public ResponseEntity<ApiResponse<OperativeResponse>> findById(@PathVariable Long id) {
        OperativeResponse response = operativeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update operative", description = "Update operative details")
    public ResponseEntity<ApiResponse<OperativeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OperativeRequest request) {
        OperativeResponse response = operativeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Operative updated successfully", response));
    }
    
    @PostMapping("/{id}/cards")
    @Operation(summary = "Add card", description = "Add CSCS card to operative")
    public ResponseEntity<ApiResponse<OperativeResponse>> addCard(
            @PathVariable Long id,
            @RequestBody Object cardData) {
        // Would call operativeService.addCard(id, cardData)
        return ResponseEntity.ok(ApiResponse.success("Card added", null));
    }
    
    @PostMapping("/{id}/cards/{cardId}/cscs-smart-check")
    @Operation(summary = "CSCS Smart Check", description = "Perform CSCS Smart Check on card")
    public ResponseEntity<ApiResponse<SubbieGateStatus>> smartCheckCard(
            @PathVariable Long id,
            @PathVariable Long cardId) {
        SubbieGateStatus response = operativeService.smartCheckCard(id, cardId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}/subbie-gate-status")
    @Operation(summary = "Get gate status", description = "Get subbie gate status for operative")
    public ResponseEntity<ApiResponse<SubbieGateStatus>> getSubbieGateStatus(@PathVariable Long id) {
        SubbieGateStatus response = operativeService.getSubbieGateStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete operative", description = "Delete an operative")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        operativeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Operative deleted", null));
    }
}