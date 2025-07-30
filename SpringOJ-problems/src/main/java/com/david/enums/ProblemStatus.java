package com.david.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProblemStatus {
    NOT_ATTEMPTED("not-attempted"),
    ATTEMPTED("attempted"),
    COMPLETED("completed");

    private final String value;

    ProblemStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ProblemStatus fromValue(String value) {
        for (ProblemStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知 status: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
