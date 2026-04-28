package com.crms.web;

import com.crms.dto.response.ApiResponse;
import com.crms.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @GetMapping
    @Operation(summary = "List documents", description = "Get paginated list of documents with optional filters")
    public ResponseEntity<ApiResponse<Object>> findAll(
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Object response = documentService.findAll(entityId, entityType, type, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a new document")
    public ResponseEntity<ApiResponse<Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "GENERAL") String type,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String metadata) {
        
        Object request = new Object();
        Object response = documentService.upload(file, request);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get document", description = "Get document metadata")
    public ResponseEntity<ApiResponse<Object>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @GetMapping("/{id}/content")
    @Operation(summary = "Download document", description = "Download document content")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        // Would retrieve document from storage and return as Resource
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{id}/versions")
    @Operation(summary = "Get versions", description = "Get document version history")
    public ResponseEntity<ApiResponse<List<Object>>> getVersions(@PathVariable Long id) {
        List<Object> versions = documentService.getVersions(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Delete a document")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}