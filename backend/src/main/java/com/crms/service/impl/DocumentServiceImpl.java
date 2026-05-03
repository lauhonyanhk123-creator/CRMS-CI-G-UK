package com.crms.service.impl;

import com.crms.dto.request.DocumentRequest;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.DocumentService;
import com.crms.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final MinioStorageService minioStorageService;
    
    private final Map<Long, DocumentMetadata> documentStore = new ConcurrentHashMap<>();
    private Long idCounter = 1L;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Object> findAll(Long entityId, String entityType, String type, int page, int size) {
        List<DocumentMetadata> filtered = documentStore.values().stream()
                .filter(doc -> entityId == null || entityId.equals(doc.getEntityId()))
                .filter(doc -> entityType == null || entityType.equals(doc.getEntityType()))
                .filter(doc -> type == null || type.equals(doc.getType()))
                .sorted(Comparator.comparing(DocumentMetadata::getUploadedAt).reversed())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<DocumentMetadata> pageContent = start < filtered.size() ? filtered.subList(start, end) : Collections.emptyList();

        List<Map<String, Object>> content = pageContent.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());

        return PageResponse.builder()
                .content(new java.util.ArrayList<>(content))
                .page(page)
                .size(size)
                .totalElements((long) filtered.size())
                .totalPages((int) Math.ceil((double) filtered.size() / size))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Object findById(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }
        return mapToResponse(document);
    }

    @Override
    @Transactional
    public Object upload(Object file, Object metadata) {
        if (!(file instanceof MultipartFile)) {
            throw new IllegalArgumentException("File must be a MultipartFile");
        }

        MultipartFile multipartFile = (MultipartFile) file;
        DocumentRequest request = metadata instanceof DocumentRequest ? (DocumentRequest) metadata : null;
        
        String type = request != null ? request.getType() : "GENERAL";
        Long entityId = request != null ? request.getEntityId() : null;
        String entityType = request != null ? request.getEntityType() : "";

        String originalFilename = multipartFile.getOriginalFilename();
        
        // Generate object ID for MinIO storage
        String objectId = minioStorageService.generateObjectId("documents", originalFilename);
        if (objectId == null) {
            objectId = "2024-01-01/documents/abc123_" + originalFilename;
        }
        String bucket = minioStorageService.getDocumentsBucket();
        if (bucket == null) {
            bucket = "crms-documents";
        }

        try {
            // Upload to MinIO
            minioStorageService.uploadFile(multipartFile, bucket, objectId);

            DocumentMetadata document = DocumentMetadata.builder()
                    .id(idCounter++)
                    .originalFilename(originalFilename)
                    .objectId(objectId)
                    .bucket(bucket)
                    .fileSize(multipartFile.getSize())
                    .contentType(multipartFile.getContentType())
                    .type(type)
                    .entityId(entityId)
                    .entityType(entityType)
                    .uploadedAt(LocalDateTime.now())
                    .uploadedBy("system")
                    .build();

            documentStore.put(document.getId(), document);
            log.info("Uploaded document {} to MinIO as {}", originalFilename, objectId);

            return mapToResponse(document);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> getVersions(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }

        Map<String, Object> version = new HashMap<>();
        version.put("id", document.getId());
        version.put("version", 1);
        version.put("filename", document.getOriginalFilename());
        version.put("uploadedAt", document.getUploadedAt());
        version.put("uploadedBy", document.getUploadedBy());
        version.put("isCurrent", true);

        return Collections.singletonList(version);
    }

    @Override
    public String getDownloadUrl(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }
        // Return pre-signed URL from MinIO
        return minioStorageService.getDownloadUrl(document.getBucket(), document.getObjectId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }

        try {
            // Delete from MinIO
            minioStorageService.deleteFile(document.getBucket(), document.getObjectId());
        } catch (Exception e) {
            log.warn("Could not delete file from MinIO: {}", e.getMessage());
        }

        documentStore.remove(id);
        log.info("Deleted document {}", id);
    }

    /**
     * Load document as InputStream for streaming response.
     */
    public InputStream loadFileAsStream(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }

        try {
            return minioStorageService.downloadFile(document.getBucket(), document.getObjectId());
        } catch (Exception e) {
            log.error("Failed to download file from MinIO", e);
            throw new ResourceNotFoundException("Document file not found", id);
        }
    }

    /**
     * Get document metadata for external access.
     */
    public DocumentMetadata getDocumentMetadata(Long id) {
        return documentStore.get(id);
    }

    private Map<String, Object> mapToResponse(DocumentMetadata document) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", document.getId());
        map.put("originalFilename", document.getOriginalFilename());
        map.put("fileSize", document.getFileSize());
        map.put("contentType", document.getContentType());
        map.put("type", document.getType());
        map.put("entityId", document.getEntityId());
        map.put("entityType", document.getEntityType());
        map.put("objectId", document.getObjectId());
        map.put("bucket", document.getBucket());
        map.put("uploadedAt", document.getUploadedAt());
        map.put("uploadedBy", document.getUploadedBy());
        map.put("downloadUrl", "/api/v1/documents/" + document.getId() + "/content");
        return map;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentMetadata {
        private Long id;
        private String originalFilename;
        private String objectId;
        private String bucket;
        private Long fileSize;
        private String contentType;
        private String type;
        private Long entityId;
        private String entityType;
        private LocalDateTime uploadedAt;
        private String uploadedBy;
    }
}
