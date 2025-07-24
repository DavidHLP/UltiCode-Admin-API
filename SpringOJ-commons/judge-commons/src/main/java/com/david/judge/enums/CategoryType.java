package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonValue;

@Getter
public enum CategoryType {
    ALGORITHMS("Algorithms", "算法"),
    DATABASE("Database", "数据库"),
    SHELL("Shell", "Shell"),
    MULTI_THREADING("Multi-threading", "多线程"),
    JAVASCRIPT("JavaScript", "JavaScript"),
    PANDAS("Pandas", "Pandas");

    @EnumValue
    @JsonValue
    private final String category;
    private final String description;

    CategoryType(String category, String description) {
        this.category = category;
        this.description = description;
    }
    public static CategoryType fromString(String category) {
        for (CategoryType js : CategoryType.values()) {
            if (js.category.equalsIgnoreCase(category)) {
                return js;
            }
        }
        throw new IllegalArgumentException("未知的类型: " + category);
    }

    @Override
    public String toString() {
        return this.category;
    }
}
