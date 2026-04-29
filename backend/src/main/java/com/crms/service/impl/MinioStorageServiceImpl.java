package com.crms.service.impl;

import com.crms.service.MinioStorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO Storage Service Implementation
 * Handles file uploads and downloads using MinIO/S3-compatible storage.
 * Supports multipart upload for files larger than 5MB using composeObject().
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.buckets.documents:crms-documents}")
    private String documentsBucket;

    @Value("${minio.buckets.images:crms-images}")
    private String imagesBucket;

    @Value("${minio.buckets.backups:crms-backups}")
    private String backupsBucket;

    // Part size for multipart upload: 5MB (MinIO default minimum)
    private static final long MULTIPART_THRESHOLD = 5 * 1024 * 1024; // 5MB
    private static final int PART_SIZE = 5 * 1024 * 1024; // 5MB per part
    private static final int MAX_PARTS = 10000;

    // Pre-signed URL expiry times
    private static final int DOWNLOAD_URL_EXPIRY_MINUTES = 15;
    private static final int UPLOAD_URL_EXPIRY_HOURS = 1;

    // Temporary directory for multipart uploads
    @Value("${app.storage.path:./uploads}")
    private String storagePath;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        try {
            ensureBucketExists(documentsBucket);
            ensureBucketExists(imagesBucket);
            ensureBucketExists(backupsBucket);
            log.info("MinIO storage initialized with buckets: documents={}, images={}, backups={}",
                    documentsBucket, imagesBucket, backupsBucket);
        } catch (Exception e) {
            log.warn("Failed to initialize MinIO buckets on startup: {}. Will retry on first use.", e.getMessage());
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists: {}", bucketName, e);
            throw new RuntimeException("Failed to initialize bucket: " + bucketName, e);
        }
    }

    /**
     * Upload a file to MinIO storage.
     * Uses multipart upload for files > 5MB using composeObject().
     *
     * @param file     The MultipartFile to upload
     * @param bucket   The target bucket
     * @param objectId The object ID/path in the bucket
     * @return The uploaded object path
     */
    public String uploadFile(MultipartFile file, String bucket, String objectId) {
        try {
            long fileSize = file.getSize();

            if (fileSize > MULTIPART_THRESHOLD) {
                return uploadMultipart(file, bucket, objectId);
            } else {
                return uploadSimple(file, bucket, objectId);
            }
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", objectId, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Simple upload for files <= 5MB
     */
    private String uploadSimple(MultipartFile file, String bucket, String objectId) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("Uploaded file (simple): {}/{}", bucket, objectId);
            return objectId;
        }
    }

    /**
     * Multipart upload for files > 5MB using composeObject().
     * Splits the file into 5MB parts and uses MinIO's composeObject for efficient upload.
     */
    private String uploadMultipart(MultipartFile file, String bucket, String objectId) throws Exception {
        log.info("Starting multipart upload for file: {} (size: {} bytes)", objectId, file.getSize());

        // Create temporary directory for parts
        Path tempDir = Paths.get(storagePath, "minio-temp", UUID.randomUUID().toString());
        Files.createDirectories(tempDir);

        try {
            // Save the file temporarily
            Path tempFile = tempDir.resolve("upload-" + UUID.randomUUID());
            file.transferTo(tempFile.toFile());

            // Get file info
            long fileSize = Files.size(tempFile);
            int numParts = (int) Math.ceil((double) fileSize / PART_SIZE);

            log.debug("File will be split into {} parts", numParts);

            // Upload each part
            List<ComposeSource> sources = new ArrayList<>();
            byte[] buffer = new byte[PART_SIZE];

            try (java.io.FileInputStream fis = new java.io.FileInputStream(tempFile.toFile())) {
                for (int i = 0; i < numParts; i++) {
                    int bytesRead = fis.read(buffer);
                    if (bytesRead <= 0) break;

                    byte[] partData = bytesRead == PART_SIZE ? buffer : Arrays.copyOf(buffer, bytesRead);

                    // Upload part to temporary object
                    String partObjectId = objectId + "/.parts/part-" + String.format("%05d", i);
                    try (ByteArrayInputStream partStream = new ByteArrayInputStream(partData)) {
                        minioClient.putObject(PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(partObjectId)
                                .stream(partStream, partData.length, -1)
                                .contentType("application/octet-stream")
                                .build());
                    }

                    sources.add(ComposeSource.builder()
                            .bucket(bucket)
                            .object(partObjectId)
                            .build());
                }
            }

            // Compose all parts into final object using composeObject()
            minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .sources(sources)
                    .contentType(file.getContentType())
                    .build());

            log.info("Completed multipart upload: {}/{} ({} parts)", bucket, objectId, numParts);

            // Clean up part objects (optional - can keep for debugging)
            for (ComposeSource source : sources) {
                try {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(source.object())
                            .build());
                } catch (Exception e) {
                    log.warn("Failed to cleanup part: {}", source.object(), e);
                }
            }

            return objectId;

        } finally {
            // Clean up temp directory
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (Exception e) {
                log.warn("Failed to cleanup temp directory: {}", tempDir, e);
            }
        }
    }

    /**
     * Upload file from local path (for larger files that may already be on disk)
     */
    public String uploadFromPath(Path localPath, String bucket, String objectId, String contentType) {
        try {
            long fileSize = Files.size(localPath);

            if (fileSize > MULTIPART_THRESHOLD) {
                return uploadMultipartFromPath(localPath, bucket, objectId, contentType);
            } else {
                try (InputStream stream = Files.newInputStream(localPath)) {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectId)
                            .stream(stream, fileSize, -1)
                            .contentType(contentType)
                            .build());
                }
                log.info("Uploaded file from path (simple): {}/{}", bucket, objectId);
                return objectId;
            }
        } catch (Exception e) {
            log.error("Failed to upload file from path: {}", localPath, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Multipart upload from local file path
     */
    private String uploadMultipartFromPath(Path localPath, String bucket, String objectId, String contentType) throws Exception {
        long fileSize = Files.size(localPath);
        int numParts = (int) Math.ceil((double) fileSize / PART_SIZE);

        log.info("Starting multipart upload from path: {} (size: {} bytes, {} parts)", objectId, fileSize, numParts);

        List<ComposeSource> sources = new ArrayList<>();

        try (java.io.FileInputStream fis = new java.io.FileInputStream(localPath.toFile())) {
            byte[] buffer = new byte[PART_SIZE];

            for (int i = 0; i < numParts; i++) {
                int bytesRead = fis.read(buffer);
                if (bytesRead <= 0) break;

                byte[] partData = bytesRead == PART_SIZE ? buffer : Arrays.copyOf(buffer, bytesRead);
                String partObjectId = objectId + "/.parts/part-" + String.format("%05d", i);

                try (ByteArrayInputStream partStream = new ByteArrayInputStream(partData)) {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(partObjectId)
                            .stream(partStream, partData.length, -1)
                            .contentType("application/octet-stream")
                            .build());
                }

                sources.add(ComposeSource.builder()
                        .bucket(bucket)
                        .object(partObjectId)
                        .build());
            }
        }

        // Compose final object
        minioClient.composeObject(ComposeObjectArgs.builder()
                .bucket(bucket)
                .object(objectId)
                .sources(sources)
                .contentType(contentType)
                .build());

        // Cleanup parts
        for (ComposeSource source : sources) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(source.object())
                        .build());
            } catch (Exception e) {
                log.warn("Failed to cleanup part: {}", source.object(), e);
            }
        }

        log.info("Completed multipart upload from path: {}/{}", bucket, objectId);
        return objectId;
    }

    /**
     * Get a pre-signed URL for downloading a file.
     * URL expires after 15 minutes.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @param expiry   Optional custom expiry in seconds (overrides default 15 min)
     * @return Pre-signed download URL
     */
    public String getDownloadUrl(String bucket, String objectId, Integer expiry) {
        try {
            int urlExpiry = expiry != null ? expiry : DOWNLOAD_URL_EXPIRY_MINUTES * 60;
            int expirySeconds = Math.min(urlExpiry, 604800); // Max 7 days per MinIO constraint

            String url = minioClient.getObjectUrl(bucket, objectId);
            // MinIO getObjectUrl doesn't generate signed URL, use presignedGetObject instead
            url = minioClient.presignedGetObject(
                    PresignedGetObjectArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectId)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build());

            log.debug("Generated download URL for {}/{} (expires in {} seconds)", bucket, objectId, expirySeconds);
            return url;
        } catch (Exception e) {
            log.error("Failed to generate download URL for {}/{}", bucket, objectId, e);
            throw new RuntimeException("Failed to generate download URL: " + e.getMessage(), e);
        }
    }

    /**
     * Get a pre-signed URL for downloading a file with default 15-minute expiry.
     */
    public String getDownloadUrl(String bucket, String objectId) {
        return getDownloadUrl(bucket, objectId, null);
    }

    /**
     * Get a pre-signed URL for uploading a file.
     * URL expires after 1 hour.
     *
     * @param bucket      The bucket name
     * @param objectId    The object ID
     * @param contentType The content type of the file
     * @param expiry      Optional custom expiry in seconds (overrides default 1 hour)
     * @return Pre-signed upload URL
     */
    public String getUploadUrl(String bucket, String objectId, String contentType, Integer expiry) {
        try {
            int urlExpiry = expiry != null ? expiry : UPLOAD_URL_EXPIRY_HOURS * 60 * 60;
            int expirySeconds = Math.min(urlExpiry, 604800); // Max 7 days per MinIO constraint

            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("content-type", contentType);

            String url = minioClient.presignedPutObject(
                    PresignedPutObjectArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectId)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .extraQueryParams(reqParams)
                            .build());

            log.debug("Generated upload URL for {}/{} (expires in {} seconds)", bucket, objectId, expirySeconds);
            return url;
        } catch (Exception e) {
            log.error("Failed to generate upload URL for {}/{}", bucket, objectId, e);
            throw new RuntimeException("Failed to generate upload URL: " + e.getMessage(), e);
        }
    }

    /**
     * Get a pre-signed URL for uploading a file with default 1-hour expiry.
     */
    public String getUploadUrl(String bucket, String objectId, String contentType) {
        return getUploadUrl(bucket, objectId, contentType, null);
    }

    /**
     * Download a file from MinIO storage.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @return InputStream of the file content
     */
    public InputStream downloadFile(String bucket, String objectId) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .build());
        } catch (Exception e) {
            log.error("Failed to download file {}/{}", bucket, objectId, e);
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * Download a file to a local path.
     */
    public void downloadToPath(String bucket, String objectId, Path localPath) {
        try {
            Files.createDirectories(localPath.getParent());
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .filename(localPath.toString())
                    .build());
            log.info("Downloaded {}/{} to {}", bucket, objectId, localPath);
        } catch (Exception e) {
            log.error("Failed to download file to path {}/{}", bucket, objectId, e);
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from MinIO storage.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     */
    public void deleteFile(String bucket, String objectId) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .build());
            log.info("Deleted file: {}/{}", bucket, objectId);
        } catch (Exception e) {
            log.error("Failed to delete file {}/{}", bucket, objectId, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Check if an object exists in the bucket.
     */
    public boolean objectExists(String bucket, String objectId) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get object metadata/stat.
     */
    public StatObjectResponse getObjectStat(String bucket, String objectId) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectId)
                    .build());
        } catch (Exception e) {
            log.error("Failed to get object stat {}/{}", bucket, objectId, e);
            throw new RuntimeException("Failed to get object info: " + e.getMessage(), e);
        }
    }

    /**
     * Get the documents bucket name.
     */
    public String getDocumentsBucket() {
        return documentsBucket;
    }

    /**
     * Get the images bucket name.
     */
    public String getImagesBucket() {
        return imagesBucket;
    }

    /**
     * Get the backups bucket name.
     */
    public String getBackupsBucket() {
        return backupsBucket;
    }

    /**
     * Generate a unique object ID based on date and random UUID.
     */
    public String generateObjectId(String prefix, String filename) {
        String date = java.time.LocalDate.now().toString();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("%s/%s/%s_%s", date, prefix, uuid, sanitizedFilename);
    }
}
