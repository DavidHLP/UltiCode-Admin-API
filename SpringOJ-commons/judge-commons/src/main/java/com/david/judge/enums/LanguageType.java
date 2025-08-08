package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 编程语言类型枚举
 */
@Getter
public enum LanguageType {
    JAVA("Java", ".java", "java"),
    PYTHON("Python", ".py", "python"),
    CPP("C++", ".cpp", "cpp"),
    C("C", ".c", "c"),
    GO("Go", ".go", "go"),
    JAVASCRIPT("JavaScript", ".js", "javascript"),
    TYPESCRIPT("TypeScript", ".ts", "typescript"),
    RUST("Rust", ".rs", "rust"),
    KOTLIN("Kotlin", ".kt", "kotlin"),
    SCALA("Scala", ".scala", "scala"),
    RUBY("Ruby", ".rb", "ruby"),
    PHP("PHP", ".php", "php"),
    CSHARP("C#", ".cs", "csharp");

    @EnumValue
    private final String name;
    private final String suffix;
    private final String code;

    LanguageType(String name, String suffix, String code) {
        this.name = name;
        this.suffix = suffix;
        this.code = code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LanguageType fromString(String code) {
        for (LanguageType type : LanguageType.values()) {
            if (type.code.equalsIgnoreCase(code) || type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的编程语言: " + code);
    }

    @JsonValue
    public String getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
