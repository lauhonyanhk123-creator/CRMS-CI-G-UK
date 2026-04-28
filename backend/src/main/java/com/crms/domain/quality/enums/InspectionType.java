package com.crms.domain.quality.enums;

public enum InspectionType {
    WITNESS("Witness"),
    HOLD("Hold"),
    MONITOR("Monitor");

    private final String displayName;

    InspectionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
