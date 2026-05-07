package com.crms.licence;

import com.crms.domain.user.repository.UserRepository;
import com.crms.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LicenceProperties props;

    @InjectMocks
    private LicenceService licenceService;

    @BeforeEach
    void setUp() {
        when(props.getTier()).thenReturn(LicenceProperties.Tier.YARD);
        when(props.resolvedMaxUsers()).thenReturn(15);
        when(props.getInstallationId()).thenReturn("TEST-INST-001");
        when(props.isEnforcementEnabled()).thenReturn(true);
        when(props.getExpiryDate()).thenReturn(null);
    }

    // ── currentStatus() ───────────────────────────────────────────────────────

    @Test
    void currentStatus_returnsCorrectCounts() {
        when(userRepository.countByEnabledTrue()).thenReturn(8L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.getActiveUsers()).isEqualTo(8);
        assertThat(status.getMaxUsers()).isEqualTo(15);
        assertThat(status.getAvailableSlots()).isEqualTo(7);
        assertThat(status.isAtCapacity()).isFalse();
    }

    @Test
    void currentStatus_atCapacity_whenActiveEqualsMax() {
        when(userRepository.countByEnabledTrue()).thenReturn(15L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.isAtCapacity()).isTrue();
        assertThat(status.getAvailableSlots()).isEqualTo(0);
    }

    @Test
    void currentStatus_availableSlots_neverNegative() {
        // Over-cap scenario (shouldn't happen in practice but defensive)
        when(userRepository.countByEnabledTrue()).thenReturn(20L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.getAvailableSlots()).isEqualTo(0);
    }

    @Test
    void currentStatus_tierAndInstallationId_arePresent() {
        when(userRepository.countByEnabledTrue()).thenReturn(0L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.getTier()).isEqualTo("YARD");
        assertThat(status.getInstallationId()).isEqualTo("TEST-INST-001");
    }

    @Test
    void currentStatus_maintenanceExpired_whenExpiryInPast() {
        when(props.getExpiryDate()).thenReturn(LocalDate.now().minusDays(1));
        when(userRepository.countByEnabledTrue()).thenReturn(5L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.isMaintenanceExpired()).isTrue();
    }

    @Test
    void currentStatus_maintenanceNotExpired_whenExpiryInFuture() {
        when(props.getExpiryDate()).thenReturn(LocalDate.now().plusDays(90));
        when(userRepository.countByEnabledTrue()).thenReturn(5L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.isMaintenanceExpired()).isFalse();
    }

    @Test
    void currentStatus_noExpiry_maintenanceNotExpired() {
        when(props.getExpiryDate()).thenReturn(null);
        when(userRepository.countByEnabledTrue()).thenReturn(5L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.isMaintenanceExpired()).isFalse();
        assertThat(status.getDaysUntilExpiry()).isEqualTo(-1);
    }

    @Test
    void currentStatus_summaryContainsTierAndUserCount() {
        when(userRepository.countByEnabledTrue()).thenReturn(5L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.getSummary()).contains("YARD");
        assertThat(status.getSummary()).contains("5");
        assertThat(status.getSummary()).contains("15");
    }

    @Test
    void currentStatus_summaryIndicatesCapacity_whenFull() {
        when(userRepository.countByEnabledTrue()).thenReturn(15L);

        LicenceStatus status = licenceService.currentStatus();

        assertThat(status.getSummary()).containsIgnoringCase("CAPACITY");
    }

    // ── assertUserSlotAvailable() ─────────────────────────────────────────────

    @Test
    void assertUserSlotAvailable_doesNotThrow_whenBelowCap() {
        when(userRepository.countByEnabledTrue()).thenReturn(10L);

        assertThatNoException().isThrownBy(() -> licenceService.assertUserSlotAvailable());
    }

    @Test
    void assertUserSlotAvailable_throws_whenAtCap() {
        when(userRepository.countByEnabledTrue()).thenReturn(15L);

        assertThatThrownBy(() -> licenceService.assertUserSlotAvailable())
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("User limit reached")
                .hasMessageContaining("YARD");
    }

    @Test
    void assertUserSlotAvailable_throws_whenOverCap() {
        when(userRepository.countByEnabledTrue()).thenReturn(16L);

        assertThatThrownBy(() -> licenceService.assertUserSlotAvailable())
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void assertUserSlotAvailable_skips_whenEnforcementDisabled() {
        when(props.isEnforcementEnabled()).thenReturn(false);
        // Even at cap, no exception when enforcement is off
        when(userRepository.countByEnabledTrue()).thenReturn(15L);

        assertThatNoException().isThrownBy(() -> licenceService.assertUserSlotAvailable());
        verify(userRepository, never()).countByEnabledTrue();
    }

    @Test
    void assertUserSlotAvailable_errorMessageContainsUpgradeHint() {
        when(userRepository.countByEnabledTrue()).thenReturn(15L);

        assertThatThrownBy(() -> licenceService.assertUserSlotAvailable())
                .hasMessageContaining("upgrade");
    }

    // ── Tier defaults ─────────────────────────────────────────────────────────

    @Test
    void tierDefaults_yard_15users() {
        assertThat(LicenceProperties.Tier.YARD.getDefaultMaxUsers()).isEqualTo(15);
    }

    @Test
    void tierDefaults_site_40users() {
        assertThat(LicenceProperties.Tier.SITE.getDefaultMaxUsers()).isEqualTo(40);
    }

    @Test
    void tierDefaults_group_100users() {
        assertThat(LicenceProperties.Tier.GROUP.getDefaultMaxUsers()).isEqualTo(100);
    }

    @Test
    void resolvedMaxUsers_usesExplicitOverride_whenSet() {
        LicenceProperties p = new LicenceProperties();
        p.setTier(LicenceProperties.Tier.YARD);
        p.setMaxUsers(20); // override above tier default

        assertThat(p.resolvedMaxUsers()).isEqualTo(20);
    }

    @Test
    void resolvedMaxUsers_usesTierDefault_whenMaxUsersIsZero() {
        LicenceProperties p = new LicenceProperties();
        p.setTier(LicenceProperties.Tier.SITE);
        p.setMaxUsers(0);

        assertThat(p.resolvedMaxUsers()).isEqualTo(40);
    }
}
