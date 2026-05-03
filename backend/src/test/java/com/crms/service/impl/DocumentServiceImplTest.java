package com.crms.service.impl;

import com.crms.dto.request.DocumentRequest;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.MinioStorageService;
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

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentServiceImpl.
 * Tests cover document CRUD operations, file upload/download, and metadata management.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentServiceImplTest {

    @Mock
    private MinioStorageService minioStorageService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private MockMultipartFile testFile;
    private DocumentRequest documentRequest;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test file content".getBytes()
        );

        documentRequest = DocumentRequest.builder()
                .type("CONTRACT")
                .entityId(1L)
                .entityType("CONTRACT")
                .build();
    }

    // ================================================================
    // CRUD OPERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("CRUD Operation Tests")
    class CrudOperationTests {

        @Test
        @DisplayName("findAll returns empty page when no documents")
        void findAll_returnsEmptyPage_whenNoDocuments() {
            // When
            PageResponse<Object> response = documentService.findAll(null, null, null, 0, 10);

            // Then
            assertNotNull(response);
            assertTrue(response.getContent().isEmpty());
            assertEquals(0, response.getPage());
            assertEquals(10, response.getSize());
            assertEquals(0L, response.getTotalElements());
        }

        @Test
        @DisplayName("findAll returns paginated documents")
        void findAll_returnsPaginatedDocuments() throws Exception {
            // Given - Upload two documents first
            documentService.upload(testFile, documentRequest);
            
            MockMultipartFile secondFile = new MockMultipartFile(
                    "file",
                    "second-document.pdf",
                    "application/pdf",
                    "Second file content".getBytes()
            );
            documentService.upload(secondFile, documentRequest);

            // When
            PageResponse<Object> response = documentService.findAll(1L, "CONTRACT", "CONTRACT", 0, 10);

            // Then
            assertNotNull(response);
            assertEquals(2, response.getContent().size());
        }

        @Test
        @DisplayName("findAll filters by entityId")
        void findAll_filtersByEntityId() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            
            DocumentRequest differentEntity = DocumentRequest.builder()
                    .type("INVOICE")
                    .entityId(2L)
                    .entityType("CONTRACT")
                    .build();
            MockMultipartFile differentFile = new MockMultipartFile(
                    "file", "different.pdf", "application/pdf", "content".getBytes()
            );
            documentService.upload(differentFile, differentEntity);

            // When
            PageResponse<Object> response = documentService.findAll(1L, null, null, 0, 10);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
        }

        @Test
        @DisplayName("findAll filters by entityType")
        void findAll_filtersByEntityType() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            
            DocumentRequest differentType = DocumentRequest.builder()
                    .type("INVOICE")
                    .entityId(1L)
                    .entityType("SITE")
                    .build();
            MockMultipartFile differentFile = new MockMultipartFile(
                    "file", "different.pdf", "application/pdf", "content".getBytes()
            );
            documentService.upload(differentFile, differentType);

            // When
            PageResponse<Object> response = documentService.findAll(null, "CONTRACT", null, 0, 10);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
        }

        @Test
        @DisplayName("findAll filters by document type")
        void findAll_filtersByDocumentType() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            
            DocumentRequest differentDocType = DocumentRequest.builder()
                    .type("INVOICE")
                    .entityId(1L)
                    .entityType("CONTRACT")
                    .build();
            MockMultipartFile differentFile = new MockMultipartFile(
                    "file", "different.pdf", "application/pdf", "content".getBytes()
            );
            documentService.upload(differentFile, differentDocType);

            // When
            PageResponse<Object> response = documentService.findAll(null, null, "CONTRACT", 0, 10);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
        }

        @Test
        @DisplayName("findAll handles pagination correctly")
        void findAll_handlesPaginationCorrectly() throws Exception {
            // Given - Upload 5 documents
            for (int i = 0; i < 5; i++) {
                MockMultipartFile file = new MockMultipartFile(
                        "file", "doc" + i + ".pdf", "application/pdf", ("content" + i).getBytes()
                );
                documentService.upload(file, documentRequest);
            }

            // When - Get page 0 with size 2
            PageResponse<Object> response = documentService.findAll(null, null, null, 0, 2);

            // Then
            assertNotNull(response);
            assertEquals(2, response.getContent().size());
            assertEquals(5L, response.getTotalElements());
            assertEquals(3, response.getTotalPages());
        }

        @Test
        @DisplayName("findById returns document when exists")
        void findById_returnsDocument_whenExists() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);

            // When
            Object response = documentService.findById(1L);

            // Then
            assertNotNull(response);
            assertTrue(response instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> docMap = (Map<String, Object>) response;
            assertEquals("test-document.pdf", docMap.get("originalFilename"));
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> documentService.findById(999L));
        }
    }

    // ================================================================
    // UPLOAD TESTS
    // ================================================================

    @Nested
    @DisplayName("Upload Tests")
    class UploadTests {

        @Test
        @DisplayName("upload saves document metadata correctly")
        void upload_savesDocumentMetadataCorrectly() throws Exception {
            // Given
            when(minioStorageService.generateObjectId(any(), any())).thenReturn("2024-01-01/documents/abc123_test.pdf");
            when(minioStorageService.getDocumentsBucket()).thenReturn("crms-documents");
            when(minioStorageService.uploadFile(any(), any(), any())).thenReturn("2024-01-01/documents/abc123_test.pdf");

            // When
            Object response = documentService.upload(testFile, documentRequest);

            // Then
            assertNotNull(response);
            assertTrue(response instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> docMap = (Map<String, Object>) response;
            assertEquals("test-document.pdf", docMap.get("originalFilename"));
            assertEquals("CONTRACT", docMap.get("type"));
            assertEquals(1L, docMap.get("entityId"));
            assertEquals("CONTRACT", docMap.get("entityType"));
            assertNotNull(docMap.get("objectId"));
            assertNotNull(docMap.get("bucket"));
            
            verify(minioStorageService).uploadFile(any(), eq("crms-documents"), any());
        }

        @Test
        @DisplayName("upload handles null metadata")
        void upload_handlesNullMetadata() throws Exception {
            // Given
            when(minioStorageService.generateObjectId(any(), any())).thenReturn("2024-01-01/documents/abc123.pdf");
            when(minioStorageService.getDocumentsBucket()).thenReturn("crms-documents");
            when(minioStorageService.uploadFile(any(), any(), any())).thenReturn("2024-01-01/documents/abc123.pdf");

            // When
            Object response = documentService.upload(testFile, null);

            // Then
            assertNotNull(response);
            assertTrue(response instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> docMap = (Map<String, Object>) response;
            assertEquals("GENERAL", docMap.get("type"));
        }

        @Test
        @DisplayName("upload throws exception for non-MultipartFile")
        void upload_throwsException_forNonMultipartFile() {
            // When/Then
            assertThrows(IllegalArgumentException.class, 
                    () -> documentService.upload("not a file", documentRequest));
        }

        @Test
        @DisplayName("upload handles MinioStorageService exception")
        void upload_handlesMinioStorageServiceException() {
            // Given
            when(minioStorageService.generateObjectId(any(), any())).thenReturn("test-id");
            when(minioStorageService.getDocumentsBucket()).thenReturn("crms-documents");
            when(minioStorageService.uploadFile(any(), any(), any()))
                    .thenThrow(new RuntimeException("MinIO connection failed"));

            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> documentService.upload(testFile, documentRequest));
            assertTrue(exception.getMessage().contains("Failed to store file"));
        }
    }

    // ================================================================
    // DOWNLOAD URL TESTS
    // ================================================================

    @Nested
    @DisplayName("Download URL Tests")
    class DownloadUrlTests {

        @Test
        @DisplayName("getDownloadUrl returns pre-signed URL")
        void getDownloadUrl_returnsPresignedUrl() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            String expectedUrl = "http://minio:9000/crms-documents/test-id?signature=abc";
            when(minioStorageService.getDownloadUrl(any(), any())).thenReturn(expectedUrl);

            // When
            String url = documentService.getDownloadUrl(1L);

            // Then
            assertNotNull(url);
            assertEquals(expectedUrl, url);
            verify(minioStorageService).getDownloadUrl("crms-documents", "2024-01-01/documents/abc123_test-document.pdf");
        }

        @Test
        @DisplayName("getDownloadUrl throws exception when document not found")
        void getDownloadUrl_throwsException_whenNotFound() {
            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> documentService.getDownloadUrl(999L));
        }
    }

    // ================================================================
    // DELETE TESTS
    // ================================================================

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("delete removes document from store and MinIO")
        void delete_removesDocumentFromStoreAndMinIO() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            doNothing().when(minioStorageService).deleteFile(any(), any());

            // When
            documentService.delete(1L);

            // Then
            verify(minioStorageService).deleteFile(any(), any());
            assertThrows(ResourceNotFoundException.class, () -> documentService.findById(1L));
        }

        @Test
        @DisplayName("delete handles MinIO deletion failure gracefully")
        void delete_handlesMinioDeletionFailure() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            doThrow(new RuntimeException("MinIO delete failed"))
                    .when(minioStorageService).deleteFile(any(), any());

            // When
            documentService.delete(1L);

            // Then - Document should still be removed from store
            assertThrows(ResourceNotFoundException.class, () -> documentService.findById(1L));
        }

        @Test
        @DisplayName("delete throws exception when document not found")
        void delete_throwsException_whenNotFound() {
            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> documentService.delete(999L));
        }
    }

    // ================================================================
    // VERSION TESTS
    // ================================================================

    @Nested
    @DisplayName("Version Tests")
    class VersionTests {

        @Test
        @DisplayName("getVersions returns version list")
        void getVersions_returnsVersionList() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);

            // When
            List<Object> versions = documentService.getVersions(1L);

            // Then
            assertNotNull(versions);
            assertEquals(1, versions.size());
            assertTrue(versions.get(0) instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> version = (Map<String, Object>) versions.get(0);
            assertEquals(1, version.get("version"));
            assertTrue((Boolean) version.get("isCurrent"));
        }

        @Test
        @DisplayName("getVersions throws exception when document not found")
        void getVersions_throwsException_whenNotFound() {
            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> documentService.getVersions(999L));
        }
    }

    // ================================================================
    // FILE STREAM TESTS
    // ================================================================

    @Nested
    @DisplayName("File Stream Tests")
    class FileStreamTests {

        @Test
        @DisplayName("loadFileAsStream returns InputStream from MinIO")
        void loadFileAsStream_returnsInputStream() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            ByteArrayInputStream expectedStream = new ByteArrayInputStream("file content".getBytes());
            when(minioStorageService.downloadFile(any(), any())).thenReturn(expectedStream);

            // When
            ByteArrayInputStream result = (ByteArrayInputStream) documentService.loadFileAsStream(1L);

            // Then
            assertNotNull(result);
            verify(minioStorageService).downloadFile(any(), any());
        }

        @Test
        @DisplayName("loadFileAsStream throws exception when document not found")
        void loadFileAsStream_throwsException_whenNotFound() {
            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> documentService.loadFileAsStream(999L));
        }

        @Test
        @DisplayName("loadFileAsStream throws exception when MinIO download fails")
        void loadFileAsStream_throwsException_whenMinioDownloadFails() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);
            when(minioStorageService.downloadFile(any(), any()))
                    .thenThrow(new RuntimeException("MinIO download failed"));

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> documentService.loadFileAsStream(1L));
        }
    }

    // ================================================================
    // METADATA ACCESS TESTS
    // ================================================================

    @Nested
    @DisplayName("Metadata Access Tests")
    class MetadataAccessTests {

        @Test
        @DisplayName("getDocumentMetadata returns metadata when exists")
        void getDocumentMetadata_returnsMetadata_whenExists() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);

            // When
            DocumentServiceImpl.DocumentMetadata metadata = documentService.getDocumentMetadata(1L);

            // Then
            assertNotNull(metadata);
            assertEquals("test-document.pdf", metadata.getOriginalFilename());
            assertEquals("CONTRACT", metadata.getType());
        }

        @Test
        @DisplayName("getDocumentMetadata returns null when not found")
        void getDocumentMetadata_returnsNull_whenNotFound() {
            // When
            DocumentServiceImpl.DocumentMetadata metadata = documentService.getDocumentMetadata(999L);

            // Then
            assertNull(metadata);
        }
    }

    // ================================================================
    // RESPONSE MAPPING TESTS
    // ================================================================

    @Nested
    @DisplayName("Response Mapping Tests")
    class ResponseMappingTests {

        @Test
        @DisplayName("mapToResponse includes download URL")
        void mapToResponse_includesDownloadUrl() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);

            // When
            Object response = documentService.findById(1L);

            // Then
            assertTrue(response instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> docMap = (Map<String, Object>) response;
            assertTrue(docMap.containsKey("downloadUrl"));
            assertTrue(((String) docMap.get("downloadUrl")).contains("/api/v1/documents/"));
        }

        @Test
        @DisplayName("mapToResponse includes all required fields")
        void mapToResponse_includesAllRequiredFields() throws Exception {
            // Given
            documentService.upload(testFile, documentRequest);

            // When
            Object response = documentService.findById(1L);

            // Then
            assertTrue(response instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> docMap = (Map<String, Object>) response;
            
            assertTrue(docMap.containsKey("id"));
            assertTrue(docMap.containsKey("originalFilename"));
            assertTrue(docMap.containsKey("fileSize"));
            assertTrue(docMap.containsKey("contentType"));
            assertTrue(docMap.containsKey("type"));
            assertTrue(docMap.containsKey("entityId"));
            assertTrue(docMap.containsKey("entityType"));
            assertTrue(docMap.containsKey("objectId"));
            assertTrue(docMap.containsKey("bucket"));
            assertTrue(docMap.containsKey("uploadedAt"));
            assertTrue(docMap.containsKey("uploadedBy"));
            assertTrue(docMap.containsKey("downloadUrl"));
        }
    }
}
