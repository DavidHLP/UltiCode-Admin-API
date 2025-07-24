package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 题目难度枚举
 */
@Getter
public enum ProblemDifficulty {
    EASY("Easy", "简单", 1),
    MEDIUM("Medium", "中等", 2),
    HARD("Hard", "困难", 3);

    @EnumValue
    @JsonValue
    private final String level;
    private final String description;
    private final int value;

    ProblemDifficulty(String level, String description, int value) {
        this.level = level;
        this.description = description;
        this.value = value;
    }

    public static ProblemDifficulty fromString(String level) {
        for (ProblemDifficulty difficulty : ProblemDifficulty.values()) {
            if (difficulty.level.equalsIgnoreCase(level)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("未知的题目难度: " + level);
    }

    @Override
    public String toString() {
        return this.level;
    }
}