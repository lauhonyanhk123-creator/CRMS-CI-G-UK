package com.crms.licence;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Licence configuration for the installed CRMS instance.
 *
 * Tiers:
 *   YARD  — up to 15 enabled users  (£18k perpetual)
 *   SITE  — up to 40 enabled users  (£36k perpetual)
 *   GROUP — up to 100 enabled users (£72k perpetual)
 *
 * Override via environment variables:
 *   CRMS_LICENCE_TIER=SITE
 *   CRMS_LICENCE_MAX_USERS=40
 *   CRMS_LICENCE_INSTALLATION_ID=INS-XXXX-XXXX
 *   CRMS_LICENCE_EXPIRY_DATE=2027-12-31
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "crms.licence")
public class LicenceProperties {

    /** Installed tier. Defaults to YARD (safest — smallest cap). */
    private Tier tier = Tier.YARD;

    /**
     * Maximum number of simultaneously enabled users.
     * When 0 the tier default is used; set explicitly to override.
     */
    private int maxUsers = 0;

    /** Unique installation identifier printed in the UI and support requests. */
    private String installationId = "UNLICENSED";

    /**
     * Perpetual licence expiry date for the annual support & maintenance contract.
     * Null means no expiry check (development / perpetual with no maintenance).
     */
    private LocalDate expiryDate;

    /** When true the licence check emits a warning instead of blocking. Useful in dev. */
    private boolean enforcementEnabled = true;

    public enum Tier {
        YARD(15),
        SITE(40),
        GROUP(100);

        private final int defaultMaxUsers;

        Tier(int defaultMaxUsers) {
            this.defaultMaxUsers = defaultMaxUsers;
        }

        public int getDefaultMaxUsers() {
            return defaultMaxUsers;
        }
    }

    /** Resolved user cap: explicit maxUsers takes precedence over tier default. */
    public int resolvedMaxUsers() {
        return maxUsers > 0 ? maxUsers : tier.getDefaultMaxUsers();
    }
}
