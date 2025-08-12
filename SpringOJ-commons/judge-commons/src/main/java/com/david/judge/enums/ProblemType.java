package com.david.judge.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum ProblemType {
	ACM("ACM"), OI("OI");
	private final String type;
	ProblemType(String type) {
		this.type = type;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static ProblemType fromString(String value) {
		for (ProblemType t : ProblemType.values()) {
			if (t.type.equalsIgnoreCase(value)) {
				return t;
			}
		}
		throw new IllegalArgumentException("未知的输入类型: " + value);
	}

	@JsonValue
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return this.type;
	}
}
