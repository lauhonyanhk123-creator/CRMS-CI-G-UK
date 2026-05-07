package com.crms.service.impl;

import com.crms.domain.healthsafety.entity.IncidentReport;
import com.crms.domain.healthsafety.enums.IncidentStatus;
import com.crms.domain.healthsafety.enums.IncidentType;
import com.crms.domain.healthsafety.enums.Severity;
import com.crms.domain.healthsafety.repository.IncidentReportRepository;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.IncidentReportRequest;
import com.crms.dto.response.IncidentReportResponse;
import com.crms.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link IncidentReportServiceImpl}.
 *
 * <p>Covers the full lifecycle of an IncidentReport: creation, submission,
 * RIDDOR notification, and closure.  All repository dependencies are mocked;
 * no Spring context is loaded.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IncidentReportServiceImpl")
class IncidentReportServiceImplTest {

    @Mock
    private IncidentReportRepository incidentReportRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private OperativeRepository operativeRepository;

    @InjectMocks
    private IncidentReportServiceImpl incidentReportService;

    // Shared fixtures
    private Site testSite;
    private Operative testOperative;
    private IncidentReport testReport;
    private IncidentReportRequest baseRequest;

    @BeforeEach
    void setUp() {
        testSite = Site.builder()
                .id(10L)
                .name("Heathrow Terminal 5")
                .siteCode("HT5-001")
                .build();

        testOperative = Operative.builder()
                .id(20L)
                .firstName("John")
                .lastName("Smith")
                .employeeRef("OP-001")
                .build();

        testReport = IncidentReport.builder()
                .id(1L)
                .site(testSite)
                .reportNumber("INC-111111")
                .incidentDate(LocalDateTime.now())
                .type(IncidentType.MINOR_INJURY)
                .severity(Severity.LOW)
                .description("Worker slipped on wet surface")
                .status(IncidentStatus.DRAFT)
                .build();

        baseRequest = IncidentReportRequest.builder()
                .siteId(10L)
                .incidentDate(LocalDateTime.now())
                .type(IncidentType.MINOR_INJURY)
                .severity(Severity.LOW)
                .description("Worker slipped on wet surface")
                .build();

        // Default save answer — return the entity as-is so mapToResponse can read fields
        when(incidentReportRepository.save(any(IncidentReport.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // create
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates report in DRAFT status linked to the requested site")
        void validRequest_returnsDraftReportLinkedToSite() {
            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));

            IncidentReportResponse response = incidentReportService.create(baseRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(IncidentStatus.DRAFT);
            assertThat(response.getSiteId()).isEqualTo(10L);
            assertThat(response.getSiteName()).isEqualTo("Heathrow Terminal 5");
            verify(incidentReportRepository).save(any(IncidentReport.class));
        }

        @Test
        @DisplayName("generates a report number with the INC- prefix")
        void validRequest_reportNumberHasIncPrefix() {
            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));

            IncidentReportResponse response = incidentReportService.create(baseRequest);

            assertThat(response.getReportNumber()).startsWith("INC-");
        }

        @Test
        @DisplayName("links operative to the report when operativeId is provided")
        void requestWithOperativeId_linksOperative() {
            IncidentReportRequest requestWithOperative = IncidentReportRequest.builder()
                    .siteId(10L)
                    .operativeId(20L)
                    .incidentDate(LocalDateTime.now())
                    .type(IncidentType.MINOR_INJURY)
                    .severity(Severity.MEDIUM)
                    .description("Worker struck by falling object")
                    .build();

            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));
            when(operativeRepository.findById(20L)).thenReturn(Optional.of(testOperative));

            IncidentReportResponse response = incidentReportService.create(requestWithOperative);

            assertThat(response.getOperativeId()).isEqualTo(20L);
            assertThat(response.getOperativeName()).isEqualTo("John Smith");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the site does not exist")
        void siteNotFound_throwsResourceNotFoundException() {
            when(siteRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentReportService.create(baseRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Site");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the operative does not exist")
        void operativeNotFound_throwsResourceNotFoundException() {
            IncidentReportRequest requestWithBadOperative = IncidentReportRequest.builder()
                    .siteId(10L)
                    .operativeId(999L)
                    .incidentDate(LocalDateTime.now())
                    .type(IncidentType.NEAR_MISS)
                    .severity(Severity.LOW)
                    .description("Near-miss at loading bay")
                    .build();

            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));
            when(operativeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentReportService.create(requestWithBadOperative))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Operative");
        }

        @Test
        @DisplayName("does not perform an operative lookup when operativeId is null")
        void nullOperativeId_noOperativeLookup() {
            // baseRequest has no operativeId
            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));

            incidentReportService.create(baseRequest);

            verify(operativeRepository, never()).findById(any());
        }
    }

    // =========================================================================
    // submit
    // =========================================================================

    @Nested
    @DisplayName("submit()")
    class Submit {

        @Test
        @DisplayName("transitions status from DRAFT to SUBMITTED")
        void draftReport_setsSubmittedStatus() {
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            IncidentReportResponse response = incidentReportService.submit(1L);

            assertThat(response.getStatus()).isEqualTo(IncidentStatus.SUBMITTED);
        }

        @Test
        @DisplayName("persists the status change via repository save")
        void validReport_savesEntityOnceWithNewStatus() {
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            incidentReportService.submit(1L);

            verify(incidentReportRepository, times(1)).save(testReport);
            assertThat(testReport.getStatus()).isEqualTo(IncidentStatus.SUBMITTED);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the report does not exist")
        void notFound_throwsResourceNotFoundException() {
            when(incidentReportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentReportService.submit(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    // submitRIDDOR
    // =========================================================================

    @Nested
    @DisplayName("submitRIDDOR()")
    class SubmitRIDDOR {

        @Test
        @DisplayName("sets reportedToHse=true and stores the hseRef on the report")
        void validReport_setsHseFieldsCorrectly() {
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            IncidentReportResponse response =
                    incidentReportService.submitRIDDOR(1L, "HSE-2025-0042");

            assertThat(response.getReportedToHse()).isTrue();
            assertThat(response.getHseRef()).isEqualTo("HSE-2025-0042");
        }

        @Test
        @DisplayName("persists the RIDDOR changes via repository save")
        void validReport_savesEntity() {
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            incidentReportService.submitRIDDOR(1L, "HSE-2025-0042");

            verify(incidentReportRepository).save(testReport);
            assertThat(testReport.getReportedToHse()).isTrue();
            assertThat(testReport.getHseRef()).isEqualTo("HSE-2025-0042");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the report does not exist")
        void notFound_throwsResourceNotFoundException() {
            when(incidentReportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentReportService.submitRIDDOR(999L, "HSE-REF"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("overrides an existing hseRef with the newly supplied value")
        void existingHseRef_isReplacedWithNewRef() {
            testReport.setHseRef("OLD-REF");
            testReport.setReportedToHse(false);
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            IncidentReportResponse response =
                    incidentReportService.submitRIDDOR(1L, "NEW-REF-999");

            assertThat(response.getHseRef()).isEqualTo("NEW-REF-999");
            assertThat(response.getReportedToHse()).isTrue();
        }
    }

    // =========================================================================
    // close
    // =========================================================================

    @Nested
    @DisplayName("close()")
    class Close {

        @Test
        @DisplayName("transitions status to CLOSED from SUBMITTED")
        void submittedReport_setsClosedStatus() {
            testReport.setStatus(IncidentStatus.SUBMITTED);
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            IncidentReportResponse response = incidentReportService.close(1L);

            assertThat(response.getStatus()).isEqualTo(IncidentStatus.CLOSED);
        }

        @Test
        @DisplayName("transitions status to CLOSED even from DRAFT")
        void draftReport_setsClosedStatus() {
            // testReport is already DRAFT
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            IncidentReportResponse response = incidentReportService.close(1L);

            assertThat(response.getStatus()).isEqualTo(IncidentStatus.CLOSED);
        }

        @Test
        @DisplayName("persists the CLOSED status via repository save")
        void validReport_savesEntityWithClosedStatus() {
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            incidentReportService.close(1L);

            verify(incidentReportRepository).save(testReport);
            assertThat(testReport.getStatus()).isEqualTo(IncidentStatus.CLOSED);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the report does not exist")
        void notFound_throwsResourceNotFoundException() {
            when(incidentReportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentReportService.close(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    // findById
    // =========================================================================

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("returns a correctly mapped response when the report exists")
        void existingReport_returnsMappedResponse() {
            when(incidentReportRepository.findById(1L)).thenReturn(Optional.of(testReport));

            IncidentReportResponse response = incidentReportService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getReportNumber()).isEqualTo("INC-111111");
            assertThat(response.getSiteId()).isEqualTo(10L);
            assertThat(response.getStatus()).isEqualTo(IncidentStatus.DRAFT);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the report does not exist")
        void notFound_throwsResourceNotFoundException() {
            when(incidentReportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentReportService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }
}
