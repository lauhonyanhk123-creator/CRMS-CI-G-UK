package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.enums.CompanyStatus;
import com.crms.domain.company.enums.CompanyType;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.enums.SiteStatus;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.SiteRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SiteResponse;
import com.crms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SiteServiceImpl.
 * Tests cover site CRUD operations, pagination, and filtering.
 */
@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private SiteServiceImpl siteService;

    private Site testSite;
    private Company testCompany;
    private SiteRequest siteRequest;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .name("Test Client Ltd")
                .companyType(CompanyType.CLIENT)
                .status(CompanyStatus.ACTIVE)
                .build();

        testSite = Site.builder()
                .id(1L)
                .name("Test Site")
                .siteCode("SITE001")
                .gridReference("SU 12345 67890")
                .postcode("AB12 3CD")
                .client(testCompany)
                .status(SiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 1, 1))
                .estimatedCompletionDate(LocalDate.of(2024, 12, 31))
                .notes("Test notes")
                .build();

        siteRequest = SiteRequest.builder()
                .name("Test Site")
                .siteCode("SITE001")
                .gridReference("SU 12345 67890")
                .postcode("AB12 3CD")
                .clientId(1L)
                .status(SiteStatus.ACTIVE)
                .startDate(LocalDate.of(2024, 1, 1))
                .estimatedCompletionDate(LocalDate.of(2024, 12, 31))
                .notes("Test notes")
                .build();
    }

    // ================================================================
    // CRUD OPERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("CRUD Operation Tests")
    class CrudOperationTests {

        @Test
        @DisplayName("findAll returns paginated sites")
        void findAll_returnsPagedSites() {
            // Given
            Page<Site> sitePage = new PageImpl<>(List.of(testSite));
            when(siteRepository.findAll(any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Test Site", response.getContent().get(0).getName());
            assertEquals("SITE001", response.getContent().get(0).getSiteCode());
        }

        @Test
        @DisplayName("findAll filters by clientId")
        void findAll_filtersByClientId() {
            // Given
            Page<Site> sitePage = new PageImpl<>(List.of(testSite));
            when(siteRepository.findByClientId(eq(1L), any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(Map.of("clientId", "1"));

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            verify(siteRepository).findByClientId(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll filters by status")
        void findAll_filtersByStatus() {
            // Given
            Page<Site> sitePage = new PageImpl<>(List.of(testSite));
            when(siteRepository.findByStatus(eq("ACTIVE"), any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(Map.of("status", "ACTIVE"));

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            verify(siteRepository).findByStatus(eq("ACTIVE"), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll uses custom pagination")
        void findAll_usesCustomPagination() {
            // Given
            Page<Site> sitePage = new PageImpl<>(List.of(testSite), 
                    org.springframework.data.domain.PageRequest.of(2, 10), 30);
            when(siteRepository.findAll(any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(
                    Map.of("page", "2", "size", "10", "sort", "name"));

            // Then
            assertNotNull(response);
            assertEquals(2, response.getPage());
            assertEquals(10, response.getSize());
            assertEquals(30, response.getTotalElements());
            assertEquals(3, response.getTotalPages());
        }

        @Test
        @DisplayName("findAll uses default pagination when params missing")
        void findAll_usesDefaultPagination() {
            // Given
            Page<Site> sitePage = new PageImpl<>(List.of(testSite));
            when(siteRepository.findAll(any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(0, response.getPage());
            assertEquals(20, response.getSize());
        }

        @Test
        @DisplayName("findById returns site when exists")
        void findById_returnsSite_whenExists() {
            // Given
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));

            // When
            SiteResponse response = siteService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("Test Site", response.getName());
            assertEquals("SITE001", response.getSiteCode());
            assertEquals("ACTIVE", response.getStatus());
            assertEquals(1L, response.getClientId());
            assertEquals("Test Client Ltd", response.getClientName());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> siteService.findById(999L));
        }

        @Test
        @DisplayName("create saves site with all fields")
        void create_savesSiteWithAllFields() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
            when(siteRepository.save(any(Site.class))).thenAnswer(invocation -> {
                Site site = invocation.getArgument(0);
                site.setId(1L);
                return site;
            });

            // When
            SiteResponse response = siteService.create(siteRequest);

            // Then
            assertNotNull(response);
            assertEquals("Test Site", response.getName());
            assertEquals("SITE001", response.getSiteCode());
            assertEquals("ACTIVE", response.getStatus());
            assertEquals("SU 12345 67890", response.getGridReference());
            verify(siteRepository).save(any(Site.class));
        }

        @Test
        @DisplayName("create throws exception when client not found")
        void create_throwsException_whenClientNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> siteService.create(siteRequest));
        }

        @Test
        @DisplayName("update modifies site fields")
        void update_modifiesSiteFields() {
            // Given
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(siteRepository.save(any(Site.class))).thenReturn(testSite);

            SiteRequest updateRequest = SiteRequest.builder()
                    .name("Updated Site")
                    .siteCode("SITE001")
                    .clientId(1L)
                    .status(SiteStatus.COMPLETED)
                    .build();

            // When
            SiteResponse response = siteService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(siteRepository).save(any(Site.class));
        }

        @Test
        @DisplayName("update changes client when clientId provided")
        void update_changesClient_whenClientIdProvided() {
            // Given
            Company newClient = Company.builder()
                    .id(2L)
                    .name("New Client Ltd")
                    .build();
            
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(companyRepository.findById(2L)).thenReturn(Optional.of(newClient));
            when(siteRepository.save(any(Site.class))).thenReturn(testSite);

            SiteRequest updateRequest = SiteRequest.builder()
                    .name("Test Site")
                    .siteCode("SITE001")
                    .clientId(2L)
                    .build();

            // When
            SiteResponse response = siteService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(companyRepository).findById(2L);
            verify(siteRepository).save(any(Site.class));
        }

        @Test
        @DisplayName("update does not change client when same clientId")
        void update_doesNotChangeClient_whenSameClientId() {
            // Given
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(siteRepository.save(any(Site.class))).thenReturn(testSite);

            SiteRequest updateRequest = SiteRequest.builder()
                    .name("Updated Site")
                    .siteCode("SITE001")
                    .clientId(1L)
                    .build();

            // When
            siteService.update(1L, updateRequest);

            // Then
            verify(companyRepository, never()).findById(any());
        }

        @Test
        @DisplayName("update throws exception when site not found")
        void update_throwsException_whenNotFound() {
            // Given
            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class,
                    () -> siteService.update(999L, siteRequest));
        }

        @Test
        @DisplayName("delete removes site")
        void delete_removesSite() {
            // Given
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            doNothing().when(siteRepository).delete(testSite);

            // When
            siteService.delete(1L);

            // Then
            verify(siteRepository).delete(testSite);
        }

        @Test
        @DisplayName("delete throws exception when site not found")
        void delete_throwsException_whenNotFound() {
            // Given
            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> siteService.delete(999L));
        }
    }

    // ================================================================
    // MAPPING TESTS
    // ================================================================

    @Nested
    @DisplayName("Mapping Tests")
    class MappingTests {

        @Test
        @DisplayName("mapToResponse handles null client correctly")
        void mapToResponse_handlesNullClient() {
            // Given
            Site siteWithoutClient = Site.builder()
                    .id(1L)
                    .name("Test Site")
                    .siteCode("SITE001")
                    .client(null)
                    .status(SiteStatus.TENDER)
                    .build();
            when(siteRepository.findById(1L)).thenReturn(Optional.of(siteWithoutClient));

            // When
            SiteResponse response = siteService.findById(1L);

            // Then
            assertNotNull(response);
            assertNull(response.getClientId());
            assertNull(response.getClientName());
        }

        @Test
        @DisplayName("mapToResponse handles null status correctly")
        void mapToResponse_handlesNullStatus() {
            // Given
            Site siteWithoutStatus = Site.builder()
                    .id(1L)
                    .name("Test Site")
                    .siteCode("SITE001")
                    .client(testCompany)
                    .status(null)
                    .build();
            when(siteRepository.findById(1L)).thenReturn(Optional.of(siteWithoutStatus));

            // When
            SiteResponse response = siteService.findById(1L);

            // Then
            assertNotNull(response);
            assertNull(response.getStatus());
        }

        @Test
        @DisplayName("mapToResponse maps dates correctly")
        void mapToResponse_mapsDatesCorrectly() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate completionDate = LocalDate.of(2024, 12, 31);
            LocalDate estimatedDate = LocalDate.of(2024, 6, 30);
            
            testSite.setStartDate(startDate);
            testSite.setCompletionDate(completionDate);
            testSite.setEstimatedCompletionDate(estimatedDate);
            
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));

            // When
            SiteResponse response = siteService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals(startDate, response.getStartDate());
            assertEquals(completionDate, response.getCompletionDate());
            assertEquals(estimatedDate, response.getEstimatedCompletionDate());
        }
    }

    // ================================================================
    // PAGINATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("PageResponse contains correct metadata")
        void pageResponse_containsCorrectMetadata() {
            // Given
            Page<Site> sitePage = new PageImpl<>(
                    List.of(testSite),
                    org.springframework.data.domain.PageRequest.of(0, 10),
                    100
            );
            when(siteRepository.findAll(any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(Map.of());

            // Then
            assertEquals(0, response.getPage());
            assertEquals(10, response.getSize());
            assertEquals(100L, response.getTotalElements());
            assertEquals(10, response.getTotalPages());
            assertEquals(1, response.getContent().size());
        }

        @Test
        @DisplayName("PageResponse calculates totalPages correctly")
        void pageResponse_calculatesTotalPagesCorrectly() {
            // Given - 25 elements, page size 10 = 3 pages
            Page<Site> sitePage = new PageImpl<>(
                    List.of(testSite),
                    org.springframework.data.domain.PageRequest.of(0, 10),
                    25
            );
            when(siteRepository.findAll(any(Pageable.class))).thenReturn(sitePage);

            // When
            PageResponse<SiteResponse> response = siteService.findAll(Map.of());

            // Then
            assertEquals(25L, response.getTotalElements());
            assertEquals(3, response.getTotalPages());
        }
    }
}
