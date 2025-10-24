package com.david.contest.enums;

import java.util.Locale;

public enum ContestRegistrationSource {
    SELF("self"),
    INVITE("invite"),
    ADMIN("admin");

    private final String code;

    ContestRegistrationSource(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ContestRegistrationSource fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SELF;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (ContestRegistrationSource source : values()) {
            if (source.code.equals(normalized)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unsupported registration source: " + code);
    }
}
