package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.healthsafety.entity.CDMRegister;
import com.crms.domain.healthsafety.repository.CDMRegisterRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.CDMRegisterRequest;
import com.crms.dto.response.CDMRegisterResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CDMRegisterServiceImpl}.
 *
 * Covers: findAll pagination, findById, create (with/without site), update (field
 * mutation, client change, site change), submitToHSE, createHealthSafetyFile,
 * completeHealthSafetyFile, findByNotificationNumber, findActiveByClientId,
 * findExpiringProjects, and findPendingHseNotification.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CDMRegisterServiceImplTest {

    // -------------------------------------------------------------------------
    //  Mocks
    // -------------------------------------------------------------------------

    @Mock
    private CDMRegisterRepository cdmRegisterRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private CDMRegisterServiceImpl service;

    // -------------------------------------------------------------------------
    //  Shared fixtures
    // -------------------------------------------------------------------------

    private Company client;
    private Site site;
    private CDMRegister register;

    @BeforeEach
    void setUpSharedFixtures() {
        client = new Company();
        client.setId(10L);
        client.setName("Acme Builders Ltd");

        site = new Site();
        site.setId(20L);
        site.setName("Site Alpha");

        register = CDMRegister.builder()
                .id(1L)
                .notificationNumber("CDM-111")
                .projectName("New Office Block")
                .projectAddress("123 High Street, London")
                .projectDescription("Five-storey commercial build")
                .client(client)
                .site(site)
                .principalDesignerName("Jane Designer")
                .principalDesignerEmail("jane@design.co.uk")
                .principalContractorName("Bob Builder")
                .principalContractorEmail("bob@build.co.uk")
                .notificationDate(LocalDate.of(2025, 1, 15))
                .constructionStartDate(LocalDate.of(2025, 3, 1))
                .constructionEndDate(LocalDate.of(2026, 6, 30))
                .isNotifiable(true)
                .moreThan30Days(true)
                .moreThan500PersonDays(false)
                .isActive(true)
                .build();
    }

    // =========================================================================
    //  findAll()
    // =========================================================================

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("returns mapped page content with default params when none supplied")
        void findAll_noParams_returnsDefaultPage() {
            Page<CDMRegister> page = new PageImpl<>(List.of(register));
            when(cdmRegisterRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<CDMRegisterResponse> result = service.findAll(Map.of());

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNotificationNumber()).isEqualTo("CDM-111");
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("passes custom page/size/sort params to repository")
        void findAll_withCustomParams_usesRequestedPagination() {
            Page<CDMRegister> page = new PageImpl<>(List.of(register));
            when(cdmRegisterRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<CDMRegisterResponse> result = service.findAll(
                    Map.of("page", "2", "size", "5", "sort", "projectName"));

            assertThat(result.getPage()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            verify(cdmRegisterRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("maps client name correctly into response")
        void findAll_mapsClientNameIntoResponse() {
            Page<CDMRegister> page = new PageImpl<>(List.of(register));
            when(cdmRegisterRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<CDMRegisterResponse> result = service.findAll(Map.of());

            CDMRegisterResponse response = result.getContent().get(0);
            assertThat(response.getClientId()).isEqualTo(10L);
            assertThat(response.getClientName()).isEqualTo("Acme Builders Ltd");
        }

        @Test
        @DisplayName("returns empty content list when repository returns no records")
        void findAll_emptyRepository_returnsEmptyContent() {
            Page<CDMRegister> emptyPage = new PageImpl<>(Collections.emptyList());
            when(cdmRegisterRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            PageResponse<CDMRegisterResponse> result = service.findAll(Map.of());

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =========================================================================
    //  findById()
    // =========================================================================

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("returns response when register exists")
        void findById_exists_returnsResponse() {
            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));

            CDMRegisterResponse result = service.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProjectName()).isEqualTo("New Office Block");
            assertThat(result.getSiteId()).isEqualTo(20L);
            assertThat(result.getSiteName()).isEqualTo("Site Alpha");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when register not found")
        void findById_notFound_throwsResourceNotFoundException() {
            when(cdmRegisterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  create()
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        private CDMRegisterRequest baseRequest() {
            return CDMRegisterRequest.builder()
                    .projectName("New Office Block")
                    .projectAddress("123 High Street")
                    .clientId(10L)
                    .principalDesignerName("Jane Designer")
                    .principalContractorName("Bob Builder")
                    .constructionStartDate(LocalDate.of(2025, 3, 1))
                    .constructionEndDate(LocalDate.of(2026, 6, 30))
                    .isNotifiable(true)
                    .moreThan30Days(true)
                    .moreThan500PersonDays(false)
                    .build();
        }

        @Test
        @DisplayName("persists register and returns response with generated CDM- notification number")
        void create_savesAndReturnsResponse() {
            when(companyRepository.findById(10L)).thenReturn(Optional.of(client));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> {
                CDMRegister r = inv.getArgument(0);
                r.setId(1L);
                return r;
            });

            CDMRegisterResponse result = service.create(baseRequest());

            assertThat(result.getProjectName()).isEqualTo("New Office Block");
            assertThat(result.getNotificationNumber()).startsWith("CDM-");
            assertThat(result.getClientId()).isEqualTo(10L);
            assertThat(result.getIsActive()).isTrue();
            verify(cdmRegisterRepository).save(any(CDMRegister.class));
        }

        @Test
        @DisplayName("sets isActive to true by default on creation")
        void create_setsIsActiveTrue() {
            when(companyRepository.findById(10L)).thenReturn(Optional.of(client));
            ArgumentCaptor<CDMRegister> captor = ArgumentCaptor.forClass(CDMRegister.class);
            when(cdmRegisterRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.create(baseRequest());

            assertThat(captor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("links site when siteId is provided in request")
        void create_withSiteId_linksSite() {
            CDMRegisterRequest req = baseRequest();
            req.setSiteId(20L);
            when(companyRepository.findById(10L)).thenReturn(Optional.of(client));
            when(siteRepository.findById(20L)).thenReturn(Optional.of(site));
            ArgumentCaptor<CDMRegister> captor = ArgumentCaptor.forClass(CDMRegister.class);
            when(cdmRegisterRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.create(req);

            assertThat(captor.getValue().getSite()).isNotNull();
            assertThat(captor.getValue().getSite().getId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("does not query siteRepository when siteId is null")
        void create_withoutSiteId_doesNotQuerySiteRepository() {
            when(companyRepository.findById(10L)).thenReturn(Optional.of(client));
            when(cdmRegisterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.create(baseRequest()); // siteId is null in baseRequest

            verify(siteRepository, never()).findById(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when client not found")
        void create_clientNotFound_throwsResourceNotFoundException() {
            when(companyRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(baseRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("10");

            verify(cdmRegisterRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when siteId provided but site not found")
        void create_siteNotFound_throwsResourceNotFoundException() {
            CDMRegisterRequest req = baseRequest();
            req.setSiteId(999L);
            when(companyRepository.findById(10L)).thenReturn(Optional.of(client));
            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  update()
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        private CDMRegisterRequest updateRequest() {
            return CDMRegisterRequest.builder()
                    .projectName("Updated Office Block")
                    .projectAddress("456 New Street")
                    .clientId(10L)
                    .principalDesignerName("Updated Designer")
                    .constructionStartDate(LocalDate.of(2025, 4, 1))
                    .constructionEndDate(LocalDate.of(2026, 12, 31))
                    .isNotifiable(false)
                    .moreThan30Days(false)
                    .moreThan500PersonDays(false)
                    .build();
        }

        @Test
        @DisplayName("updates and saves modified fields")
        void update_modifiesFields_andSaves() {
            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> inv.getArgument(0));

            CDMRegisterResponse result = service.update(1L, updateRequest());

            assertThat(result.getProjectName()).isEqualTo("Updated Office Block");
            assertThat(result.getProjectAddress()).isEqualTo("456 New Street");
            verify(cdmRegisterRepository).save(any(CDMRegister.class));
        }

        @Test
        @DisplayName("updates client when clientId changes")
        void update_withNewClientId_updatesClient() {
            Company newClient = new Company();
            newClient.setId(99L);
            newClient.setName("New Client Ltd");

            CDMRegisterRequest req = updateRequest();
            req.setClientId(99L);

            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));
            when(companyRepository.findById(99L)).thenReturn(Optional.of(newClient));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> inv.getArgument(0));

            CDMRegisterResponse result = service.update(1L, req);

            assertThat(result.getClientId()).isEqualTo(99L);
            assertThat(result.getClientName()).isEqualTo("New Client Ltd");
        }

        @Test
        @DisplayName("updates site when siteId is provided")
        void update_withSiteId_updatesSite() {
            Site newSite = new Site();
            newSite.setId(55L);
            newSite.setName("Site Beta");

            CDMRegisterRequest req = updateRequest();
            req.setSiteId(55L);

            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));
            when(siteRepository.findById(55L)).thenReturn(Optional.of(newSite));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> inv.getArgument(0));

            CDMRegisterResponse result = service.update(1L, req);

            assertThat(result.getSiteId()).isEqualTo(55L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when register not found")
        void update_notFound_throwsResourceNotFoundException() {
            when(cdmRegisterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(999L, updateRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  submitToHSE()
    // =========================================================================

    @Nested
    @DisplayName("submitToHSE()")
    class SubmitToHSETests {

        @Test
        @DisplayName("sets hseNotificationRef starting with HSE- and saves")
        void submitToHSE_setsHseRefAndSaves() {
            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> inv.getArgument(0));

            CDMRegisterResponse result = service.submitToHSE(1L);

            assertThat(result.getHseNotificationRef()).startsWith("HSE-");
            verify(cdmRegisterRepository).save(any(CDMRegister.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when register not found")
        void submitToHSE_notFound_throwsResourceNotFoundException() {
            when(cdmRegisterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.submitToHSE(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  createHealthSafetyFile()
    // =========================================================================

    @Nested
    @DisplayName("createHealthSafetyFile()")
    class CreateHealthSafetyFileTests {

        @Test
        @DisplayName("sets healthSafetyFileRef (HSF- prefix) and createdDate then saves")
        void createHealthSafetyFile_setsRefAndCreatedDate() {
            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> inv.getArgument(0));

            CDMRegisterResponse result = service.createHealthSafetyFile(1L);

            assertThat(result.getHealthSafetyFileRef()).startsWith("HSF-");
            assertThat(result.getHealthSafetyFileCreatedDate()).isNotNull();
            verify(cdmRegisterRepository).save(any(CDMRegister.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when register not found")
        void createHealthSafetyFile_notFound_throwsResourceNotFoundException() {
            when(cdmRegisterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createHealthSafetyFile(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  completeHealthSafetyFile()
    // =========================================================================

    @Nested
    @DisplayName("completeHealthSafetyFile()")
    class CompleteHealthSafetyFileTests {

        @Test
        @DisplayName("sets healthSafetyFileCompletedDate and saves")
        void completeHealthSafetyFile_setsCompletedDate() {
            when(cdmRegisterRepository.findById(1L)).thenReturn(Optional.of(register));
            when(cdmRegisterRepository.save(any(CDMRegister.class))).thenAnswer(inv -> inv.getArgument(0));

            CDMRegisterResponse result = service.completeHealthSafetyFile(1L);

            assertThat(result.getHealthSafetyFileCompletedDate()).isNotNull();
            verify(cdmRegisterRepository).save(any(CDMRegister.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when register not found")
        void completeHealthSafetyFile_notFound_throwsResourceNotFoundException() {
            when(cdmRegisterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.completeHealthSafetyFile(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  findByNotificationNumber()
    // =========================================================================

    @Nested
    @DisplayName("findByNotificationNumber()")
    class FindByNotificationNumberTests {

        @Test
        @DisplayName("returns response when notification number exists")
        void findByNotificationNumber_found_returnsResponse() {
            when(cdmRegisterRepository.findByNotificationNumber("CDM-111"))
                    .thenReturn(Optional.of(register));

            CDMRegisterResponse result = service.findByNotificationNumber("CDM-111");

            assertThat(result).isNotNull();
            assertThat(result.getNotificationNumber()).isEqualTo("CDM-111");
        }

        @Test
        @DisplayName("returns null when notification number does not exist")
        void findByNotificationNumber_notFound_returnsNull() {
            when(cdmRegisterRepository.findByNotificationNumber("CDM-UNKNOWN"))
                    .thenReturn(Optional.empty());

            CDMRegisterResponse result = service.findByNotificationNumber("CDM-UNKNOWN");

            assertThat(result).isNull();
        }
    }

    // =========================================================================
    //  findActiveByClientId()
    // =========================================================================

    @Nested
    @DisplayName("findActiveByClientId()")
    class FindActiveByClientIdTests {

        @Test
        @DisplayName("returns page response wrapping all active registers for the client")
        void findActiveByClientId_returnsWrappedList() {
            when(cdmRegisterRepository.findActiveByClientId(10L)).thenReturn(List.of(register));

            PageResponse<CDMRegisterResponse> result = service.findActiveByClientId(10L);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty page response when no active registers found")
        void findActiveByClientId_noRegisters_returnsEmptyPage() {
            when(cdmRegisterRepository.findActiveByClientId(10L)).thenReturn(Collections.emptyList());

            PageResponse<CDMRegisterResponse> result = service.findActiveByClientId(10L);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =========================================================================
    //  findExpiringProjects()
    // =========================================================================

    @Nested
    @DisplayName("findExpiringProjects()")
    class FindExpiringProjectsTests {

        @Test
        @DisplayName("delegates to repository with provided date and maps results")
        void findExpiringProjects_delegatesAndMaps() {
            LocalDate cutoff = LocalDate.of(2026, 6, 30);
            when(cdmRegisterRepository.findExpiringProjects(cutoff)).thenReturn(List.of(register));

            PageResponse<CDMRegisterResponse> result = service.findExpiringProjects(cutoff);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProjectName()).isEqualTo("New Office Block");
            verify(cdmRegisterRepository).findExpiringProjects(cutoff);
        }

        @Test
        @DisplayName("returns empty page when no projects expire on or before date")
        void findExpiringProjects_noMatches_returnsEmptyPage() {
            LocalDate cutoff = LocalDate.of(2024, 1, 1);
            when(cdmRegisterRepository.findExpiringProjects(cutoff)).thenReturn(Collections.emptyList());

            PageResponse<CDMRegisterResponse> result = service.findExpiringProjects(cutoff);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =========================================================================
    //  findPendingHseNotification()
    // =========================================================================

    @Nested
    @DisplayName("findPendingHseNotification()")
    class FindPendingHseNotificationTests {

        @Test
        @DisplayName("returns all notifiable registers without an HSE ref")
        void findPendingHseNotification_returnsCorrectRegisters() {
            when(cdmRegisterRepository.findPendingHseNotification()).thenReturn(List.of(register));

            PageResponse<CDMRegisterResponse> result = service.findPendingHseNotification();

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            verify(cdmRegisterRepository).findPendingHseNotification();
        }

        @Test
        @DisplayName("returns empty page when no pending notifications exist")
        void findPendingHseNotification_noPending_returnsEmptyPage() {
            when(cdmRegisterRepository.findPendingHseNotification()).thenReturn(Collections.emptyList());

            PageResponse<CDMRegisterResponse> result = service.findPendingHseNotification();

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}
