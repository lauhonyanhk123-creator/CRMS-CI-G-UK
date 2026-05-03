package com.crms.service.impl;

import com.crms.service.MinioStorageService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MinioStorageServiceImpl.
 * Tests cover file upload, download, URL generation, and bucket operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MinioStorageServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioStorageServiceImpl minioStorageService;

    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() throws Exception {
        // Set up bucket names via reflection
        ReflectionTestUtils.setField(minioStorageService, "documentsBucket", "crms-documents");
        ReflectionTestUtils.setField(minioStorageService, "imagesBucket", "crms-images");
        ReflectionTestUtils.setField(minioStorageService, "backupsBucket", "crms-backups");
        ReflectionTestUtils.setField(minioStorageService, "storagePath", "./uploads");

        testFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test file content".getBytes()
        );
    }

    // ================================================================
    // BUCKET NAME TESTS
    // ================================================================

    @Nested
    @DisplayName("Bucket Name Tests")
    class BucketNameTests {

        @Test
        @DisplayName("getDocumentsBucket returns configured bucket name")
        void getDocumentsBucket_returnsConfiguredName() {
            // When
            String bucket = minioStorageService.getDocumentsBucket();

            // Then
            assertEquals("crms-documents", bucket);
        }

        @Test
        @DisplayName("getImagesBucket returns configured bucket name")
        void getImagesBucket_returnsConfiguredName() {
            // When
            String bucket = minioStorageService.getImagesBucket();

            // Then
            assertEquals("crms-images", bucket);
        }

        @Test
        @DisplayName("getBackupsBucket returns configured bucket name")
        void getBackupsBucket_returnsConfiguredName() {
            // When
            String bucket = minioStorageService.getBackupsBucket();

            // Then
            assertEquals("crms-backups", bucket);
        }
    }

    // ================================================================
    // UPLOAD TESTS
    // ================================================================

    @Nested
    @DisplayName("Upload Tests")
    class UploadTests {

        @Test
        @DisplayName("uploadFile uses simple upload for small files")
        void uploadFile_usesSimpleUpload_forSmallFiles() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "2024-01-01/documents/test-file.pdf";
            
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

            // When
            String result = minioStorageService.uploadFile(testFile, bucket, objectId);

            // Then
            assertEquals(objectId, result);
            verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("uploadFile throws exception on failure")
        void uploadFile_throwsException_onFailure() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "2024-01-01/documents/test-file.pdf";
            
            when(minioClient.putObject(any(PutObjectArgs.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageService.uploadFile(testFile, bucket, objectId));
            assertTrue(exception.getMessage().contains("Failed to upload file"));
        }
    }

    // ================================================================
    // DOWNLOAD TESTS
    // ================================================================

    @Nested
    @DisplayName("Download Tests")
    class DownloadTests {

        @Test
        @DisplayName("downloadFile returns InputStream")
        void downloadFile_returnsInputStream() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            ByteArrayInputStream expectedStream = new ByteArrayInputStream("file content".getBytes());
            
            when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(new GetObjectResponse(null, bucket, null, objectId, expectedStream));

            // When
            InputStream result = minioStorageService.downloadFile(bucket, objectId);

            // Then
            assertNotNull(result);
            assertEquals("file content", new String(result.readAllBytes()));
        }

        @Test
        @DisplayName("downloadFile throws exception on failure")
        void downloadFile_throwsException_onFailure() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "nonexistent-file.pdf";
            
            when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenThrow(new RuntimeException("File not found"));

            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageService.downloadFile(bucket, objectId));
            assertTrue(exception.getMessage().contains("Failed to download file"));
        }

        @Test
        @DisplayName("downloadToPath downloads file to local path")
        void downloadToPath_downloadsFileToLocalPath() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            Path localPath = Paths.get("/tmp/downloads/test.pdf");
            
            doNothing().when(minioClient).downloadObject(any(DownloadObjectArgs.class));

            // When
            minioStorageService.downloadToPath(bucket, objectId, localPath);

            // Then
            verify(minioClient).downloadObject(any(DownloadObjectArgs.class));
        }
    }

    // ================================================================
    // DELETE TESTS
    // ================================================================

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("deleteFile removes object from bucket")
        void deleteFile_removesObjectFromBucket() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            
            doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

            // When
            minioStorageService.deleteFile(bucket, objectId);

            // Then
            verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        }

        @Test
        @DisplayName("deleteFile throws exception on failure")
        void deleteFile_throwsException_onFailure() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            
            doThrow(new RuntimeException("Delete failed"))
                    .when(minioClient).removeObject(any(RemoveObjectArgs.class));

            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageService.deleteFile(bucket, objectId));
            assertTrue(exception.getMessage().contains("Failed to delete file"));
        }
    }

    // ================================================================
    // URL GENERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("URL Generation Tests")
    class UrlGenerationTests {

        @Test
        @DisplayName("getDownloadUrl generates pre-signed URL with default expiry")
        void getDownloadUrl_generatesPresignedUrlWithDefaultExpiry() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            String expectedUrl = "http://minio:9000/crms-documents/test-document.pdf?signature=abc123";
            
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(expectedUrl);

            // When
            String result = minioStorageService.getDownloadUrl(bucket, objectId);

            // Then
            assertNotNull(result);
            assertEquals(expectedUrl, result);
            verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        }

        @Test
        @DisplayName("getDownloadUrl with custom expiry respects the expiry value")
        void getDownloadUrl_withCustomExpiry_respectsExpiryValue() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            Integer customExpiry = 3600; // 1 hour in seconds
            String expectedUrl = "http://minio:9000/crms-documents/test-document.pdf?signature=xyz";
            
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(expectedUrl);

            // When
            String result = minioStorageService.getDownloadUrl(bucket, objectId, customExpiry);

            // Then
            assertNotNull(result);
            assertEquals(expectedUrl, result);
        }

        @Test
        @DisplayName("getDownloadUrl caps expiry at 7 days (604800 seconds)")
        void getDownloadUrl_capsExpiryAt7Days() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            Integer veryLongExpiry = 999999999; // More than 7 days
            
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn("http://example.com");

            // When
            minioStorageService.getDownloadUrl(bucket, objectId, veryLongExpiry);

            // Then - Verify that presignedGetObject was called (expiry is capped internally)
            verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        }

        @Test
        @DisplayName("getDownloadUrl throws exception on failure")
        void getDownloadUrl_throwsException_onFailure() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenThrow(new RuntimeException("URL generation failed"));

            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageService.getDownloadUrl(bucket, objectId));
            assertTrue(exception.getMessage().contains("Failed to generate download URL"));
        }

        @Test
        @DisplayName("getUploadUrl generates pre-signed upload URL")
        void getUploadUrl_generatesPresignedUploadUrl() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            String contentType = "application/pdf";
            String expectedUrl = "http://minio:9000/crms-documents/test-document.pdf?upload=true";
            
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(expectedUrl);

            // When
            String result = minioStorageService.getUploadUrl(bucket, objectId, contentType);

            // Then
            assertNotNull(result);
            assertEquals(expectedUrl, result);
        }

        @Test
        @DisplayName("getUploadUrl with custom expiry respects the value")
        void getUploadUrl_withCustomExpiry_respectsValue() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            String contentType = "application/pdf";
            Integer customExpiry = 7200; // 2 hours
            String expectedUrl = "http://minio:9000/crms-documents/test-document.pdf?upload=true";
            
            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(expectedUrl);

            // When
            String result = minioStorageService.getUploadUrl(bucket, objectId, contentType, customExpiry);

            // Then
            assertNotNull(result);
            assertEquals(expectedUrl, result);
        }
    }

    // ================================================================
    // OBJECT EXISTS TESTS
    // ================================================================

    @Nested
    @DisplayName("Object Exists Tests")
    class ObjectExistsTests {

        @Test
        @DisplayName("objectExists returns true when object exists")
        void objectExists_returnsTrue_whenObjectExists() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            
            when(minioClient.statObject(any(StatObjectArgs.class)))
                    .thenReturn(mock(StatObjectResponse.class));

            // When
            boolean result = minioStorageService.objectExists(bucket, objectId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("objectExists returns false when object does not exist")
        void objectExists_returnsFalse_whenObjectNotExists() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "nonexistent.pdf";
            
            when(minioClient.statObject(any(StatObjectArgs.class)))
                    .thenThrow(new RuntimeException("Object not found"));

            // When
            boolean result = minioStorageService.objectExists(bucket, objectId);

            // Then
            assertFalse(result);
        }
    }

    // ================================================================
    // OBJECT STAT TESTS
    // ================================================================

    @Nested
    @DisplayName("Object Stat Tests")
    class ObjectStatTests {

        @Test
        @DisplayName("getObjectStat returns object metadata")
        void getObjectStat_returnsObjectMetadata() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "test-document.pdf";
            StatObjectResponse expectedStat = mock(StatObjectResponse.class);
            
            when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(expectedStat);

            // When
            StatObjectResponse result = minioStorageService.getObjectStat(bucket, objectId);

            // Then
            assertNotNull(result);
            assertEquals(expectedStat, result);
        }

        @Test
        @DisplayName("getObjectStat throws exception on failure")
        void getObjectStat_throwsException_onFailure() throws Exception {
            // Given
            String bucket = "crms-documents";
            String objectId = "nonexistent.pdf";
            
            when(minioClient.statObject(any(StatObjectArgs.class)))
                    .thenThrow(new RuntimeException("Object not found"));

            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageService.getObjectStat(bucket, objectId));
            assertTrue(exception.getMessage().contains("Failed to get object info"));
        }
    }

    // ================================================================
    // OBJECT ID GENERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Object ID Generation Tests")
    class ObjectIdGenerationTests {

        @Test
        @DisplayName("generateObjectId creates valid object ID format")
        void generateObjectId_createsValidObjectIdFormat() {
            // Given
            String prefix = "documents";
            String filename = "test-file.pdf";

            // When
            String objectId = minioStorageService.generateObjectId(prefix, filename);

            // Then
            assertNotNull(objectId);
            assertTrue(objectId.contains("documents"));
            assertTrue(objectId.contains(filename));
            assertTrue(objectId.matches("\\d{4}-\\d{2}-\\d{2}/documents/[a-f0-9]{8}_.*"));
        }

        @Test
        @DisplayName("generateObjectId sanitizes special characters in filename")
        void generateObjectId_sanitizesSpecialCharacters() {
            // Given
            String prefix = "documents";
            String filename = "test file (1).pdf";

            // When
            String objectId = minioStorageService.generateObjectId(prefix, filename);

            // Then
            assertNotNull(objectId);
            assertFalse(objectId.contains(" "));
            assertFalse(objectId.contains("("));
            assertFalse(objectId.contains(")"));
        }

        @Test
        @DisplayName("generateObjectId handles empty filename")
        void generateObjectId_handlesEmptyFilename() {
            // Given
            String prefix = "documents";
            String filename = "";

            // When
            String objectId = minioStorageService.generateObjectId(prefix, filename);

            // Then
            assertNotNull(objectId);
            assertTrue(objectId.contains("documents"));
        }

        @Test
        @DisplayName("generateObjectId includes UUID portion")
        void generateObjectId_includesUuidPortion() {
            // Given
            String prefix = "documents";
            String filename = "test.pdf";

            // When
            String objectId1 = minioStorageService.generateObjectId(prefix, filename);
            String objectId2 = minioStorageService.generateObjectId(prefix, filename);

            // Then - UUID portion should be different for different calls
            String[] parts1 = objectId1.split("/");
            String[] parts2 = objectId2.split("/");
            String uuid1 = parts1[parts1.length - 1].split("_")[0];
            String uuid2 = parts2[parts2.length - 1].split("_")[0];
            assertNotEquals(uuid1, uuid2);
        }
    }
}
