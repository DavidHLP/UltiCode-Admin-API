package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 编程语言类型枚举
 */
@Getter
public enum LanguageType {
    JAVA("Java", ".java", "JAVA"),
    PYTHON("Python", ".py", "PYTHON"),
    CPP("C++", ".cpp", "CPP"),
    C("C", ".c", "C"),
    GO("Go", ".go", "GO"),
    JAVASCRIPT("JavaScript", ".js", "JAVASCRIPT"),
    TYPESCRIPT("TypeScript", ".ts", "TYPESCRIPT"),
    RUST("Rust", ".rs", "RUST"),
    KOTLIN("Kotlin", ".kt", "KOTLIN"),
    SCALA("Scala", ".scala", "SCALA"),
    RUBY("Ruby", ".rb", "RUBY"),
    PHP("PHP", ".php", "PHP"),
    CSHARP("C#", ".cs", "CSHARP");

    private final String name;
    private final String suffix;
	@EnumValue
    private final String code;

    LanguageType(String name, String suffix, String code) {
        this.name = name;
        this.suffix = suffix;
        this.code = code;
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
