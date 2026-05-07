package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.healthsafety.entity.F10Notification;
import com.crms.domain.healthsafety.repository.F10NotificationRepository;
import com.crms.dto.request.F10NotificationRequest;
import com.crms.dto.response.F10NotificationResponse;
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
 * Unit tests for {@link F10NotificationServiceImpl}.
 *
 * Covers: findAll pagination, findById, create (happy path + contract-not-found),
 * update, submitToHSE (first-time + already-submitted guard, HDF auto-set),
 * acknowledgeHDF (happy path + requires-HDF guard), findActiveByContractId
 * (found + empty), findByNotificationNumber, and findExpiringNotifications.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class F10NotificationServiceImplTest {

    // -------------------------------------------------------------------------
    //  Mocks
    // -------------------------------------------------------------------------

    @Mock
    private F10NotificationRepository f10NotificationRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private F10NotificationServiceImpl service;

    // -------------------------------------------------------------------------
    //  Shared fixtures
    // -------------------------------------------------------------------------

    private Contract contract;
    private F10Notification notification;

    @BeforeEach
    void setUpSharedFixtures() {
        contract = new Contract();
        contract.setId(5L);
        contract.setContractRef("CTR-001");

        notification = F10Notification.builder()
                .id(1L)
                .contract(contract)
                .notificationNumber("F10-111111")
                .moreThan30Days(true)
                .moreThan500PersonDays(false)
                .constructionStartDate(LocalDate.of(2025, 4, 1))
                .constructionEndDate(LocalDate.of(2026, 3, 31))
                .isActive(true)
                .hdfAcknowledged(false)
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
            Page<F10Notification> page = new PageImpl<>(List.of(notification));
            when(f10NotificationRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<F10NotificationResponse> result = service.findAll(Map.of());

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNotificationNumber()).isEqualTo("F10-111111");
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("passes custom page and size to repository")
        void findAll_customParams_passesPageableToRepository() {
            Page<F10Notification> page = new PageImpl<>(List.of(notification));
            when(f10NotificationRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<F10NotificationResponse> result = service.findAll(
                    Map.of("page", "1", "size", "10"));

            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            verify(f10NotificationRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("returns empty content when repository has no records")
        void findAll_emptyRepository_returnsEmptyPage() {
            Page<F10Notification> emptyPage = new PageImpl<>(Collections.emptyList());
            when(f10NotificationRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            PageResponse<F10NotificationResponse> result = service.findAll(Map.of());

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("maps contractRef into response correctly")
        void findAll_mapsContractRefIntoResponse() {
            Page<F10Notification> page = new PageImpl<>(List.of(notification));
            when(f10NotificationRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<F10NotificationResponse> result = service.findAll(Map.of());

            assertThat(result.getContent().get(0).getContractRef()).isEqualTo("CTR-001");
            assertThat(result.getContent().get(0).getContractId()).isEqualTo(5L);
        }
    }

    // =========================================================================
    //  findById()
    // =========================================================================

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("returns response when notification exists")
        void findById_exists_returnsResponse() {
            when(f10NotificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            F10NotificationResponse result = service.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNotificationNumber()).isEqualTo("F10-111111");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when notification not found")
        void findById_notFound_throwsResourceNotFoundException() {
            when(f10NotificationRepository.findById(999L)).thenReturn(Optional.empty());

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

        private F10NotificationRequest buildRequest() {
            return F10NotificationRequest.builder()
                    .moreThan30Days(true)
                    .moreThan500PersonDays(false)
                    .constructionStartDate(LocalDate.of(2025, 4, 1))
                    .constructionEndDate(LocalDate.of(2026, 3, 31))
                    .build();
        }

        @Test
        @DisplayName("persists notification and returns response with generated F10- number")
        void create_savesAndReturnsResponse() {
            when(contractRepository.findById(5L)).thenReturn(Optional.of(contract));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(inv -> {
                F10Notification n = inv.getArgument(0);
                n.setId(1L);
                return n;
            });

            F10NotificationResponse result = service.create(5L, buildRequest());

            assertThat(result.getNotificationNumber()).startsWith("F10-");
            assertThat(result.getContractId()).isEqualTo(5L);
            assertThat(result.getIsActive()).isTrue();
            assertThat(result.getHdfAcknowledged()).isFalse();
            verify(f10NotificationRepository).save(any(F10Notification.class));
        }

        @Test
        @DisplayName("sets isActive=true and hdfAcknowledged=false on created notification")
        void create_setsDefaultFlags() {
            when(contractRepository.findById(5L)).thenReturn(Optional.of(contract));
            ArgumentCaptor<F10Notification> captor = ArgumentCaptor.forClass(F10Notification.class);
            when(f10NotificationRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.create(5L, buildRequest());

            assertThat(captor.getValue().getIsActive()).isTrue();
            assertThat(captor.getValue().getHdfAcknowledged()).isFalse();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contract not found")
        void create_contractNotFound_throwsResourceNotFoundException() {
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(99L, buildRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(f10NotificationRepository, never()).save(any());
        }
    }

    // =========================================================================
    //  update()
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        private F10NotificationRequest updateRequest() {
            return F10NotificationRequest.builder()
                    .moreThan30Days(false)
                    .moreThan500PersonDays(true)
                    .constructionStartDate(LocalDate.of(2025, 6, 1))
                    .constructionEndDate(LocalDate.of(2027, 5, 31))
                    .build();
        }

        @Test
        @DisplayName("updates fields and saves successfully")
        void update_modifiesFieldsAndSaves() {
            when(f10NotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            F10NotificationResponse result = service.update(1L, updateRequest());

            assertThat(result.getMoreThan30Days()).isFalse();
            assertThat(result.getMoreThan500PersonDays()).isTrue();
            assertThat(result.getConstructionStartDate()).isEqualTo(LocalDate.of(2025, 6, 1));
            verify(f10NotificationRepository).save(any(F10Notification.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when notification not found")
        void update_notFound_throwsResourceNotFoundException() {
            when(f10NotificationRepository.findById(999L)).thenReturn(Optional.empty());

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
        @DisplayName("sets submittedDate and HSE- confirmationNumber then saves")
        void submitToHSE_setsSubmittedDateAndConfirmationNumber() {
            when(f10NotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            F10NotificationResponse result = service.submitToHSE(1L);

            assertThat(result.getSubmittedDate()).isNotNull();
            assertThat(result.getConfirmationNumber()).startsWith("HSE-");
            verify(f10NotificationRepository).save(any(F10Notification.class));
        }

        @Test
        @DisplayName("also sets hdfSubmittedDate when notification requires HDF")
        void submitToHSE_whenRequiresHDF_setsHdfSubmittedDate() {
            // moreThan30Days=true means requiresHDF() == true
            assertThat(notification.requiresHDF()).isTrue();

            when(f10NotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            F10NotificationResponse result = service.submitToHSE(1L);

            assertThat(result.getHdfSubmittedDate()).isNotNull();
        }

        @Test
        @DisplayName("throws IllegalStateException when already submitted")
        void submitToHSE_alreadySubmitted_throwsIllegalStateException() {
            notification.setSubmittedDate(LocalDate.of(2025, 2, 1));
            when(f10NotificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> service.submitToHSE(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already been submitted");

            verify(f10NotificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when notification not found")
        void submitToHSE_notFound_throwsResourceNotFoundException() {
            when(f10NotificationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.submitToHSE(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  acknowledgeHDF()
    // =========================================================================

    @Nested
    @DisplayName("acknowledgeHDF()")
    class AcknowledgeHDFTests {

        @Test
        @DisplayName("sets hdfAcknowledged=true and records acknowledgement details")
        void acknowledgeHDF_setsAcknowledgedDetails() {
            // requiresHDF() is true because moreThan30Days=true
            when(f10NotificationRepository.findById(1L)).thenReturn(Optional.of(notification));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            F10NotificationResponse result = service.acknowledgeHDF(1L);

            assertThat(result.getHdfAcknowledged()).isTrue();
            assertThat(result.getHdfAcknowledgedBy()).isEqualTo("SYSTEM");
            assertThat(result.getHdfAcknowledgedDate()).isNotNull();
            verify(f10NotificationRepository).save(any(F10Notification.class));
        }

        @Test
        @DisplayName("throws IllegalStateException when notification does not require HDF")
        void acknowledgeHDF_notRequiresHDF_throwsIllegalStateException() {
            // Neither moreThan30Days nor moreThan500PersonDays is true
            F10Notification noHdfNotification = F10Notification.builder()
                    .id(2L)
                    .contract(contract)
                    .notificationNumber("F10-222222")
                    .moreThan30Days(false)
                    .moreThan500PersonDays(false)
                    .isActive(true)
                    .hdfAcknowledged(false)
                    .build();

            when(f10NotificationRepository.findById(2L)).thenReturn(Optional.of(noHdfNotification));

            assertThatThrownBy(() -> service.acknowledgeHDF(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("HDF acknowledgment");

            verify(f10NotificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when notification not found")
        void acknowledgeHDF_notFound_throwsResourceNotFoundException() {
            when(f10NotificationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.acknowledgeHDF(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =========================================================================
    //  findActiveByContractId()
    // =========================================================================

    @Nested
    @DisplayName("findActiveByContractId()")
    class FindActiveByContractIdTests {

        @Test
        @DisplayName("returns first active notification when records exist")
        void findActiveByContractId_returnsFirstActiveNotification() {
            when(f10NotificationRepository.findActiveByContractId(5L))
                    .thenReturn(List.of(notification));

            F10NotificationResponse result = service.findActiveByContractId(5L);

            assertThat(result.getNotificationNumber()).isEqualTo("F10-111111");
            assertThat(result.getContractId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when no active notification found")
        void findActiveByContractId_empty_throwsResourceNotFoundException() {
            when(f10NotificationRepository.findActiveByContractId(5L))
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.findActiveByContractId(5L))
                    .isInstanceOf(ResourceNotFoundException.class);
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
            when(f10NotificationRepository.findByNotificationNumber("F10-111111"))
                    .thenReturn(Optional.of(notification));

            F10NotificationResponse result = service.findByNotificationNumber("F10-111111");

            assertThat(result).isNotNull();
            assertThat(result.getNotificationNumber()).isEqualTo("F10-111111");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when notification number not found")
        void findByNotificationNumber_notFound_throwsResourceNotFoundException() {
            when(f10NotificationRepository.findByNotificationNumber("F10-UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByNotificationNumber("F10-UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    //  findExpiringNotifications()
    // =========================================================================

    @Nested
    @DisplayName("findExpiringNotifications()")
    class FindExpiringNotificationsTests {

        @Test
        @DisplayName("delegates to repository with provided date and maps results")
        void findExpiringNotifications_delegatesAndMaps() {
            LocalDate cutoff = LocalDate.of(2026, 3, 31);
            when(f10NotificationRepository.findExpiringNotifications(cutoff))
                    .thenReturn(List.of(notification));

            PageResponse<F10NotificationResponse> result = service.findExpiringNotifications(cutoff);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNotificationNumber()).isEqualTo("F10-111111");
            assertThat(result.getTotalElements()).isEqualTo(1L);
            verify(f10NotificationRepository).findExpiringNotifications(cutoff);
        }

        @Test
        @DisplayName("returns empty page when no notifications expire on or before date")
        void findExpiringNotifications_noMatches_returnsEmptyPage() {
            LocalDate cutoff = LocalDate.of(2024, 1, 1);
            when(f10NotificationRepository.findExpiringNotifications(cutoff))
                    .thenReturn(Collections.emptyList());

            PageResponse<F10NotificationResponse> result = service.findExpiringNotifications(cutoff);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}
