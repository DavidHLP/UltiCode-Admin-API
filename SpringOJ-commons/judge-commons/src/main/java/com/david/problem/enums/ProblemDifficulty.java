package com.david.problem.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 题目难度枚举
 */
@Getter
public enum ProblemDifficulty {
	EASY("EASY", "简单"), MEDIUM("MEDIUM", "中等"), HARD("HARD", "困难");

	@EnumValue
	private final String level;
	private final String description;

	ProblemDifficulty(String level, String description) {
		this.level = level;
		this.description = description;
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

	@JsonValue
	public String getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return this.level;
	}
}