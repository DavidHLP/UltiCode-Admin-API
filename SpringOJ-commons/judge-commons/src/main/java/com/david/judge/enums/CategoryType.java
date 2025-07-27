package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonValue;

@Getter
public enum CategoryType {
    ALGORITHMS("ALGORITHMS", "算法"),
    DATABASE("DATABASE", "数据库"),
    SHELL("SHELL", "Shell"),
    MULTI_THREADING("MULTI_THREADING", "多线程"),
    JAVASCRIPT("JAVASCRIPT", "JavaScript"),
    PANDAS("PANDAS", "Pandas");

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
