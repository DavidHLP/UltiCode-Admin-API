package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 编程语言类型枚举
 */
@Getter
public enum LanguageType {
    java("Java", ".java", "java"),
    CPP("C++", ".cpp", "cpp"),
    PYTHON("Python", ".py", "python"),
    JAVASCRIPT("JavaScript", ".js", "javascript"),
    GOLANG("Go", ".go", "go"),
    RUST("Rust", ".rs", "rust");

    @EnumValue
    private final String name;
    private final String suffix;
    private final String code;

    LanguageType(String name, String suffix, String code) {
        this.name = name;
        this.suffix = suffix;
        this.code = code;
    }

    public static LanguageType fromString(String code) {
        for (LanguageType type : LanguageType.values()) {
            if (type.code.equalsIgnoreCase(code) || type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的编程语言: " + code);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
