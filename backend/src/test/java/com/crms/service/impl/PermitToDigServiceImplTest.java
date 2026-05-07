package com.crms.service.impl;

import com.crms.domain.healthsafety.entity.PermitToDig;
import com.crms.domain.healthsafety.enums.PermitStatus;
import com.crms.domain.healthsafety.repository.PermitToDigRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.PermitToDigRequest;
import com.crms.dto.response.PermitToDigResponse;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PermitToDigServiceImpl.
 * Covers permit creation, retrieval, update, status workflow transitions,
 * extension, and not-found error paths.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermitToDigServiceImplTest {

    @Mock
    private PermitToDigRepository permitToDigRepository;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private PermitToDigServiceImpl permitToDigService;

    private Site testSite;
    private PermitToDig testPermit;
    private PermitToDigRequest testRequest;

    @BeforeEach
    void setUp() {
        testSite = Site.builder()
                .id(10L)
                .name("Alpha Excavation Site")
                .siteCode("ALPHA-001")
                .build();

        testPermit = PermitToDig.builder()
                .id(1L)
                .site(testSite)
                .permitNumber("PTD-111111")
                .worksDescription("Dig trench for cable route")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .status(PermitStatus.DRAFT)
                .extensionCount(0)
                .build();

        testRequest = PermitToDigRequest.builder()
                .siteId(10L)
                .worksDescription("Dig trench for cable route")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();

        // Default save answer – return whatever entity is passed in
        when(permitToDigRepository.save(any(PermitToDig.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns response when permit exists")
        void findById_existingPermit_returnsResponse() {
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getPermitNumber()).isEqualTo("PTD-111111");
            assertThat(response.getStatus()).isEqualTo(PermitStatus.DRAFT);
            assertThat(response.getSiteId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void findById_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates permit with DRAFT status when site exists")
        void create_validRequest_returnsDraftPermit() {
            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));

            PermitToDigResponse response = permitToDigService.create(testRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(PermitStatus.DRAFT);
            assertThat(response.getSiteId()).isEqualTo(10L);
            assertThat(response.getWorksDescription()).isEqualTo("Dig trench for cable route");
            verify(permitToDigRepository).save(any(PermitToDig.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when site not found")
        void create_siteNotFound_throwsResourceNotFoundException() {
            when(siteRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.create(testRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Site");
        }

        @Test
        @DisplayName("generated permit number starts with PTD-")
        void create_validRequest_permitNumberHasPtdPrefix() {
            when(siteRepository.findById(10L)).thenReturn(Optional.of(testSite));

            PermitToDigResponse response = permitToDigService.create(testRequest);

            assertThat(response.getPermitNumber()).startsWith("PTD-");
        }
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates fields on existing permit")
        void update_existingPermit_updatesFields() {
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigRequest updatedRequest = PermitToDigRequest.builder()
                    .siteId(10L)
                    .worksDescription("Updated trench description")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(14))
                    .build();

            PermitToDigResponse response = permitToDigService.update(1L, updatedRequest);

            assertThat(response.getWorksDescription()).isEqualTo("Updated trench description");
            assertThat(response.getEndDate()).isEqualTo(LocalDate.now().plusDays(14));
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("reassigns site when siteId changes during update")
        void update_differentSiteId_reassignsSite() {
            Site newSite = Site.builder()
                    .id(20L)
                    .name("Beta Site")
                    .siteCode("BETA-001")
                    .build();

            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));
            when(siteRepository.findById(20L)).thenReturn(Optional.of(newSite));

            PermitToDigRequest requestWithNewSite = PermitToDigRequest.builder()
                    .siteId(20L)
                    .worksDescription("Work on new site")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(5))
                    .build();

            PermitToDigResponse response = permitToDigService.update(1L, requestWithNewSite);

            assertThat(response.getSiteId()).isEqualTo(20L);
            assertThat(response.getSiteName()).isEqualTo("Beta Site");
            verify(siteRepository).findById(20L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found on update")
        void update_permitNotFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.update(99L, testRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -------------------------------------------------------------------------
    // submitForPrecheck
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("submitForPrecheck")
    class SubmitForPrecheck {

        @Test
        @DisplayName("sets status to PRECHECKED and sets requestedDate")
        void submitForPrecheck_draftPermit_setsStatusPrechecked() {
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.submitForPrecheck(1L);

            assertThat(response.getStatus()).isEqualTo(PermitStatus.PRECHECKED);
            assertThat(response.getRequestedDate()).isNotNull();
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void submitForPrecheck_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.submitForPrecheck(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // precheck
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("precheck")
    class Precheck {

        @Test
        @DisplayName("sets status to PRECHECKED and records precheckedDate")
        void precheck_permit_setsStatusPrechecked() {
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.precheck(1L);

            assertThat(response.getStatus()).isEqualTo(PermitStatus.PRECHECKED);
            assertThat(response.getPrecheckedDate()).isNotNull();
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void precheck_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.precheck(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // rejectPrecheck
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("rejectPrecheck")
    class RejectPrecheck {

        @Test
        @DisplayName("sets status back to DRAFT and stores cancellation reason")
        void rejectPrecheck_storesReasonAndSetsDraft() {
            testPermit.setStatus(PermitStatus.PRECHECKED);
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.rejectPrecheck(1L, "Unsafe conditions");

            assertThat(response.getStatus()).isEqualTo(PermitStatus.DRAFT);
            assertThat(response.getCancellationReason()).isEqualTo("Unsafe conditions");
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void rejectPrecheck_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.rejectPrecheck(99L, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // issue
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("sets status to ISSUED and records issuedDate")
        void issue_permit_setsStatusIssued() {
            testPermit.setStatus(PermitStatus.PRECHECKED);
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.issue(1L);

            assertThat(response.getStatus()).isEqualTo(PermitStatus.ISSUED);
            assertThat(response.getIssuedDate()).isNotNull();
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void issue_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.issue(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // startWork
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("startWork")
    class StartWork {

        @Test
        @DisplayName("sets status to IN_PROGRESS")
        void startWork_permit_setsStatusInProgress() {
            testPermit.setStatus(PermitStatus.ISSUED);
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.startWork(1L);

            assertThat(response.getStatus()).isEqualTo(PermitStatus.IN_PROGRESS);
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void startWork_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.startWork(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // complete
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("sets status to COMPLETED and records completedDate")
        void complete_inProgressPermit_setsStatusCompleted() {
            testPermit.setStatus(PermitStatus.IN_PROGRESS);
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.complete(1L);

            assertThat(response.getStatus()).isEqualTo(PermitStatus.COMPLETED);
            assertThat(response.getCompletedDate()).isNotNull();
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void complete_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.complete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // cancel
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("sets status to CANCELLED, stores reason, and records cancelledDate")
        void cancel_permit_setsStatusCancelledWithReason() {
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.cancel(1L, "Work no longer required");

            assertThat(response.getStatus()).isEqualTo(PermitStatus.CANCELLED);
            assertThat(response.getCancellationReason()).isEqualTo("Work no longer required");
            assertThat(response.getCancelledDate()).isNotNull();
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void cancel_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.cancel(99L, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // extend
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("extend")
    class Extend {

        @Test
        @DisplayName("updates endDate, increments extensionCount, and sets lastExtensionDate")
        void extend_permit_updatesEndDateAndIncrementCount() {
            testPermit.setStatus(PermitStatus.IN_PROGRESS);
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            LocalDate newEndDate = LocalDate.now().plusDays(21);
            PermitToDigResponse response = permitToDigService.extend(1L, newEndDate);

            assertThat(response.getEndDate()).isEqualTo(newEndDate);
            assertThat(response.getExtensionCount()).isEqualTo(1);
            assertThat(response.getLastExtensionDate()).isNotNull();
            verify(permitToDigRepository).save(testPermit);
        }

        @Test
        @DisplayName("increments extensionCount on each subsequent extension")
        void extend_calledTwice_extensionCountIsTwo() {
            testPermit.setStatus(PermitStatus.IN_PROGRESS);
            testPermit.setExtensionCount(1);
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));

            LocalDate furtherDate = LocalDate.now().plusDays(30);
            PermitToDigResponse response = permitToDigService.extend(1L, furtherDate);

            assertThat(response.getExtensionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when permit not found")
        void extend_notFound_throwsResourceNotFoundException() {
            when(permitToDigRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permitToDigService.extend(99L, LocalDate.now().plusDays(7)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // findActiveBySiteId
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findActiveBySiteId")
    class FindActiveBySiteId {

        @Test
        @DisplayName("returns response when an active permit exists for the site")
        void findActiveBySiteId_found_returnsResponse() {
            testPermit.setStatus(PermitStatus.IN_PROGRESS);
            when(permitToDigRepository.findActivePermitForSite(10L))
                    .thenReturn(Optional.of(testPermit));

            PermitToDigResponse response = permitToDigService.findActiveBySiteId(10L);

            assertThat(response).isNotNull();
            assertThat(response.getSiteId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("returns null when no active permit exists for the site")
        void findActiveBySiteId_notFound_returnsNull() {
            when(permitToDigRepository.findActivePermitForSite(10L))
                    .thenReturn(Optional.empty());

            PermitToDigResponse response = permitToDigService.findActiveBySiteId(10L);

            assertThat(response).isNull();
        }
    }
}
