package com.crms.domain.quality.enums;

public enum SignOffResult {
    APPROVED("Approved"),
    CONDITIONS("Approved with Conditions"),
    REFUSED("Refused");

    private final String displayName;

    SignOffResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
