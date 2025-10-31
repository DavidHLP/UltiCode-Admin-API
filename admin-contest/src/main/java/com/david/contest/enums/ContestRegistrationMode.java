package com.david.contest.enums;

import java.util.Locale;

public enum ContestRegistrationMode {
    OPEN("open"),
    APPROVAL("approval"),
    INVITE_ONLY("invite_only");

    private final String code;

    ContestRegistrationMode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ContestRegistrationMode fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OPEN;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (ContestRegistrationMode value : values()) {
            if (value.code.equals(normalized)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported registration mode: " + code);
    }
}
