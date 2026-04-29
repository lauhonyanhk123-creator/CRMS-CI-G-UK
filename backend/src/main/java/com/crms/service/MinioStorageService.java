package com.crms.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * MinIO Storage Service Interface
 */
public interface MinioStorageService {

    /**
     * Upload a file using multipart upload for files > 5MB.
     *
     * @param file     The MultipartFile to upload
     * @param bucket   The target bucket
     * @param objectId The object ID/path in the bucket
     * @return The uploaded object path
     */
    String uploadFile(MultipartFile file, String bucket, String objectId);

    /**
     * Get a pre-signed URL for downloading (expires in 15 minutes).
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @return Pre-signed download URL
     */
    String getDownloadUrl(String bucket, String objectId);

    /**
     * Get a pre-signed URL for downloading with custom expiry.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @param expiry   Custom expiry in seconds
     * @return Pre-signed download URL
     */
    String getDownloadUrl(String bucket, String objectId, Integer expiry);

    /**
     * Get a pre-signed URL for uploading (expires in 1 hour).
     *
     * @param bucket      The bucket name
     * @param objectId    The object ID
     * @param contentType The content type of the file
     * @return Pre-signed upload URL
     */
    String getUploadUrl(String bucket, String objectId, String contentType);

    /**
     * Get a pre-signed URL for uploading with custom expiry.
     *
     * @param bucket      The bucket name
     * @param objectId    The object ID
     * @param contentType The content type of the file
     * @param expiry      Custom expiry in seconds
     * @return Pre-signed upload URL
     */
    String getUploadUrl(String bucket, String objectId, String contentType, Integer expiry);

    /**
     * Download a file from MinIO storage.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @return InputStream of the file content
     */
    InputStream downloadFile(String bucket, String objectId);

    /**
     * Download a file to a local path.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @param path     Local destination path
     */
    void downloadToPath(String bucket, String objectId, Path path);

    /**
     * Upload a file from local path.
     *
     * @param localPath    Local file path
     * @param bucket       Target bucket
     * @param objectId     Target object ID
     * @param contentType  Content type
     * @return The uploaded object path
     */
    String uploadFromPath(Path localPath, String bucket, String objectId, String contentType);

    /**
     * Delete a file from MinIO storage.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     */
    void deleteFile(String bucket, String objectId);

    /**
     * Check if an object exists.
     *
     * @param bucket   The bucket name
     * @param objectId The object ID
     * @return true if exists
     */
    boolean objectExists(String bucket, String objectId);

    /**
     * Generate a unique object ID based on date and random UUID.
     *
     * @param prefix   Prefix for the object (e.g., "invoices", "photos")
     * @param filename Original filename
     * @return Generated object ID
     */
    String generateObjectId(String prefix, String filename);

    /**
     * Get the documents bucket name.
     */
    String getDocumentsBucket();

    /**
     * Get the images bucket name.
     */
    String getImagesBucket();

    /**
     * Get the backups bucket name.
     */
    String getBackupsBucket();
}
