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

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static QuestionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (QuestionStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        for (QuestionStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
