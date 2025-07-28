package com.david.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuestionStatus {
    NOT_ATTEMPTED("not-attempted"),
    ATTEMPTED("attempted"),
    COMPLETED("completed");

    private final String value;

    QuestionStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static QuestionStatus fromValue(String value) {
        for (QuestionStatus status : values()) {
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
