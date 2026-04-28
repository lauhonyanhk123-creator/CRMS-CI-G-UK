package com.crms.domain.quality.enums;

public enum InspectionResult {
    PASS("Pass"),
    FAIL("Fail"),
    NC("Non-Conformance"),
    NA("Not Applicable");

    private final String displayName;

    InspectionResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
