package com.crms.licence;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LicenceStatus {

    private String tier;
    private String installationId;
    private int maxUsers;
    private long activeUsers;
    private long availableSlots;
    private boolean atCapacity;
    private boolean maintenanceExpired;
    private LocalDate expiryDate;
    private int daysUntilExpiry;

    /** True if the product itself can still be used (perpetual licence). */
    private boolean usable;

    /** Human-readable summary for the UI. */
    private String summary;
}
