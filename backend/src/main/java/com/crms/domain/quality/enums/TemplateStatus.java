package com.crms.domain.quality.enums;

public enum TemplateStatus {
    DRAFT("Draft"),
    ACTIVE("Active"),
    ARCHIVED("Archived");

    private final String displayName;

    TemplateStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
