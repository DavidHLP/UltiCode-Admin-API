package com.david.contest.enums;

import java.util.Locale;

public enum ContestRegistrationStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CANCELLED("cancelled");

    private final String code;

    ContestRegistrationStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }

    public static ContestRegistrationStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return PENDING;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (ContestRegistrationStatus status : values()) {
            if (status.code.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported registration status: " + code);
    }
}
