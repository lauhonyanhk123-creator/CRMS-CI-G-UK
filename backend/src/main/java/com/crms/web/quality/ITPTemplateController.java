package com.crms.web.quality;

import com.crms.dto.request.quality.ITPTemplateRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPTemplateResponse;
import com.crms.service.quality.ITPTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quality/templates")
@RequiredArgsConstructor
@Tag(name = "Quality - ITP Templates", description = "ITP Template management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ITPTemplateController {

    private final ITPTemplateService service;

    @GetMapping
    @Operation(summary = "List ITP templates", description = "Get paginated list of ITP templates")
    public ResponseEntity<ApiResponse<PageResponse<ITPTemplateResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tradeCategory,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("category", category);
        params.put("tradeCategory", tradeCategory);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<ITPTemplateResponse> response = service.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get categories", description = "Get list of distinct template categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories(
            @RequestParam(required = false) String tradeCategory) {
        // This would need a custom query - simplified for now
        return ResponseEntity.ok(ApiResponse.success(List.of("Foundation", "Structure", "Drainage", "Brickwork", "Finishes")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ITP template", description = "Get ITP template by ID")
    public ResponseEntity<ApiResponse<ITPTemplateResponse>> findById(@PathVariable Long id) {
        ITPTemplateResponse response = service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create ITP template", description = "Create a new ITP template")
    public ResponseEntity<ApiResponse<ITPTemplateResponse>> create(@Valid @RequestBody ITPTemplateRequest request) {
        ITPTemplateResponse response = service.create(request);
        return ResponseEntity.ok(ApiResponse.success("ITP Template created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ITP template", description = "Update ITP template details")
    public ResponseEntity<ApiResponse<ITPTemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ITPTemplateRequest request) {
        ITPTemplateResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("ITP Template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ITP template", description = "Delete ITP template")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ITP Template deleted successfully", null));
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "Copy ITP template", description = "Create a copy of an existing template")
    public ResponseEntity<ApiResponse<ITPTemplateResponse>> copyTemplate(@PathVariable Long id) {
        ITPTemplateResponse response = service.copyTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("ITP Template copied successfully", response));
    }
}
