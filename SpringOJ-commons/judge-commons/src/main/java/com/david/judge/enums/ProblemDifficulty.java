package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
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
	private final String level;
	private final String description;
	private final int value;

	@JsonValue
	public String getLevel() {
		return level;
	}

	ProblemDifficulty(String level, String description, int value) {
		this.level = level;
		this.description = description;
		this.value = value;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
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