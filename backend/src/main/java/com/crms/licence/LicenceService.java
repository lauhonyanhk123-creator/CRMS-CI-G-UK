package com.crms.licence;

import com.crms.domain.user.repository.UserRepository;
import com.crms.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenceService {

    private final LicenceProperties props;
    private final UserRepository userRepository;

    /**
     * Build the current licence status snapshot.
     * Called by the /licence endpoint and on startup logging.
     */
    public LicenceStatus currentStatus() {
        long activeUsers = userRepository.countByEnabledTrue();
        int maxUsers = props.resolvedMaxUsers();
        long available = Math.max(0, maxUsers - activeUsers);
        boolean atCapacity = activeUsers >= maxUsers;

        LocalDate expiry = props.getExpiryDate();
        boolean maintenanceExpired = expiry != null && expiry.isBefore(LocalDate.now());
        int daysUntilExpiry = expiry != null
                ? (int) ChronoUnit.DAYS.between(LocalDate.now(), expiry)
                : Integer.MAX_VALUE;

        String summary = buildSummary(activeUsers, maxUsers, available, atCapacity,
                maintenanceExpired, daysUntilExpiry, expiry);

        return LicenceStatus.builder()
                .tier(props.getTier().name())
                .installationId(props.getInstallationId())
                .maxUsers(maxUsers)
                .activeUsers(activeUsers)
                .availableSlots(available)
                .atCapacity(atCapacity)
                .maintenanceExpired(maintenanceExpired)
                .expiryDate(expiry)
                .daysUntilExpiry(daysUntilExpiry == Integer.MAX_VALUE ? -1 : daysUntilExpiry)
                .usable(true) // perpetual licence — always usable
                .summary(summary)
                .build();
    }

    /**
     * Enforce the user cap before creating a new enabled user.
     * Throws {@link ValidationException} if the cap is reached and enforcement is on.
     */
    public void assertUserSlotAvailable() {
        if (!props.isEnforcementEnabled()) {
            return;
        }

        long activeUsers = userRepository.countByEnabledTrue();
        int maxUsers = props.resolvedMaxUsers();

        if (activeUsers >= maxUsers) {
            String msg = String.format(
                    "User limit reached for %s tier (%d/%d active users). " +
                    "Disable an existing user or upgrade your licence to add more.",
                    props.getTier().name(), activeUsers, maxUsers);
            log.warn("Licence cap enforced: {}", msg);
            throw new ValidationException(msg);
        }
    }

    /** Log a summary line at startup so operators immediately see the licence state. */
    public void logStartupSummary() {
        LicenceStatus status = currentStatus();
        log.info("CRMS Licence — installation: {} | tier: {} | users: {}/{} | maintenance: {}",
                status.getInstallationId(),
                status.getTier(),
                status.getActiveUsers(),
                status.getMaxUsers(),
                status.isMaintenanceExpired() ? "EXPIRED" : (status.getExpiryDate() != null
                        ? status.getExpiryDate().toString() : "perpetual"));
        if (status.isAtCapacity()) {
            log.warn("CRMS Licence — user cap reached ({}/{}). New user creation is blocked.",
                    status.getActiveUsers(), status.getMaxUsers());
        }
    }

    private String buildSummary(long active, int max, long available, boolean atCapacity,
                                boolean maintenanceExpired, int daysUntilExpiry, LocalDate expiry) {
        StringBuilder sb = new StringBuilder();
        sb.append(props.getTier().name()).append(" tier — ");
        sb.append(active).append("/").append(max).append(" users");
        if (atCapacity) {
            sb.append(" (AT CAPACITY)");
        } else {
            sb.append(" (").append(available).append(" slot").append(available == 1 ? "" : "s").append(" free)");
        }
        if (expiry != null) {
            if (maintenanceExpired) {
                sb.append(" | Support & maintenance expired ").append(expiry);
            } else if (daysUntilExpiry <= 30) {
                sb.append(" | Maintenance expires in ").append(daysUntilExpiry).append(" day").append(daysUntilExpiry == 1 ? "" : "s");
            } else {
                sb.append(" | Maintenance valid until ").append(expiry);
            }
        }
        return sb.toString();
    }
}
