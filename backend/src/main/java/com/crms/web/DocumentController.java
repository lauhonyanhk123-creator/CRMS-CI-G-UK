package com.crms.web;

import com.crms.dto.request.DocumentRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.service.DocumentService;
import com.crms.service.MinioStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {
    
    private final DocumentService documentService;
    private final MinioStorageService minioStorageService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List documents", description = "Get paginated list of documents with optional filters")
    public ResponseEntity<ApiResponse<PageResponse<Object>>> findAll(
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Object> response = documentService.findAll(entityId, entityType, type, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload document", description = "Upload a new document")
    public ResponseEntity<ApiResponse<Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "GENERAL") String type,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String metadata) {
        
        DocumentRequest request = DocumentRequest.builder()
                .type(type)
                .entityId(entityId)
                .entityType(entityType)
                .metadata(metadata)
                .build();
        
        Object response = documentService.upload(file, request);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get document", description = "Get document metadata")
    public ResponseEntity<ApiResponse<Object>> findById(@PathVariable Long id) {
        Object response = documentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}/content")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download document", description = "Download document content")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        // Get document metadata and storage path
        Object documentMeta = documentService.findById(id);
        
        if (documentMeta == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Extract bucket and objectId from document metadata
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> docMap = (java.util.Map<String, Object>) documentMeta;
            String bucket = minioStorageService.getDocumentsBucket();
            String objectId = (String) docMap.getOrDefault("objectId", "documents/" + id);
            
            // Get content type from document or default
            String contentType = (String) docMap.getOrDefault("contentType", "application/octal-stream");
            String filename = (String) docMap.getOrDefault("originalFilename", "document");
            
            // Get file from MinIO
            InputStream inputStream = minioStorageService.downloadFile(bucket, objectId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/versions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get versions", description = "Get document version history")
    public ResponseEntity<ApiResponse<java.util.List<Object>>> getVersions(@PathVariable Long id) {
        java.util.List<Object> versions = documentService.getVersions(id);
        return ResponseEntity.ok(ApiResponse.success(versions));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete document", description = "Delete a document")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
