package com.crms.domain.quality.enums;

public enum BuildingControlType {
    NHBC("NHBC"),
    LABC("LABC"),
    LOCAL_AUTHORITY("Local Authority"),
    OTHER("Other");

    private final String displayName;

    BuildingControlType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
