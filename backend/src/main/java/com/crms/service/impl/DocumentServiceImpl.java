package com.crms.service.impl;

import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final Map<Long, DocumentMetadata> documentStore = new ConcurrentHashMap<>();
    private Long idCounter = 1L;

    @Value("${app.storage.path:./uploads}")
    private String storagePath;

    @Override
    public PageResponse<Object> findAll(Long entityId, String entityType, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));

        List<DocumentMetadata> filtered = documentStore.values().stream()
                .filter(doc -> entityId == null || entityId.equals(doc.getEntityId()))
                .filter(doc -> entityType == null || entityType.equals(doc.getEntityType()))
                .filter(doc -> type == null || type.equals(doc.getType()))
                .sorted(Comparator.comparing(DocumentMetadata::getUploadedAt).reversed())
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<DocumentMetadata> pageContent = start < filtered.size() ? filtered.subList(start, end) : Collections.emptyList();

        List<Map<String, Object>> content = pageContent.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());

        return PageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements((long) filtered.size())
                .totalPages((int) Math.ceil((double) filtered.size() / size))
                .build();
    }

    @Override
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
        Map<String, Object> meta = metadata instanceof Map ? (Map<String, Object>) metadata : new HashMap<>();

        String originalFilename = multipartFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + extension;
        String type = meta.getOrDefault("type", "GENERAL").toString();
        Long entityId = meta.containsKey("entityId") ? Long.parseLong(meta.get("entityId").toString()) : null;
        String entityType = meta.getOrDefault("entityType", "").toString();

        try {
            Path uploadDir = Paths.get(storagePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(storedFilename);
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            DocumentMetadata document = DocumentMetadata.builder()
                    .id(idCounter++)
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .filePath(filePath.toString())
                    .fileSize(multipartFile.getSize())
                    .contentType(multipartFile.getContentType())
                    .type(type)
                    .entityId(entityId)
                    .entityType(entityType)
                    .uploadedAt(LocalDateTime.now())
                    .uploadedBy(meta.getOrDefault("uploadedBy", "system").toString())
                    .build();

            documentStore.put(document.getId(), document);
            log.info("Uploaded document {} -> {}", originalFilename, storedFilename);

            return mapToResponse(document);
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public List<Object> getVersions(Long id) {
        // For this simple implementation, return the document itself as the only version
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
        return "/api/v1/documents/" + id + "/content";
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }

        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file from disk: {}", e.getMessage());
        }

        documentStore.remove(id);
        log.info("Deleted document {}", id);
    }

    public Resource loadFileAsResource(Long id) {
        DocumentMetadata document = documentStore.get(id);
        if (document == null) {
            throw new ResourceNotFoundException("Document", id);
        }

        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Document file not found", id);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Document file not found", id);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
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
        map.put("uploadedAt", document.getUploadedAt());
        map.put("uploadedBy", document.getUploadedBy());
        map.put("downloadUrl", "/api/v1/documents/" + document.getId() + "/content");
        return map;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DocumentMetadata {
        private Long id;
        private String originalFilename;
        private String storedFilename;
        private String filePath;
        private Long fileSize;
        private String contentType;
        private String type;
        private Long entityId;
        private String entityType;
        private LocalDateTime uploadedAt;
        private String uploadedBy;
    }
}
