package com.david.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum CategoryType {
	ALGORITHMS("ALGORITHMS", "算法"),
	DATABASE("DATABASE", "数据库"),
	SHELL("SHELL", "Shell"),
	MULTI_THREADING("MULTI_THREADING", "多线程"),
	JAVASCRIPT("JAVASCRIPT", "JavaScript"),
	PANDAS("PANDAS", "Pandas");

	@EnumValue
	private final String category;
	private final String description;

	CategoryType(String category, String description) {
		this.category = category;
		this.description = description;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static CategoryType fromString(String category) {
		for (CategoryType js : CategoryType.values()) {
			if (js.category.equalsIgnoreCase(category)) {
				return js;
			}
		}
		throw new IllegalArgumentException("未知的类型: " + category);
	}

	@JsonValue
	public String getCategory() {
		return category;
	}

	@Override
	public String toString() {
		return this.category;
	}
}
