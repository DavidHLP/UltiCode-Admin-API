package com.david.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 判题状态枚举
 */
@Getter
public enum JudgeStatus {
	PENDING("PENDING", "等待判题"),
	JUDGING("JUDGING", "判题中"),
	ACCEPTED("ACCEPTED", "答案正确"),
	CONTINUE("CONTINUE", "继续判题"),
	WRONG_ANSWER("WRONG_ANSWER", "答案错误"),
	TIME_LIMIT_EXCEEDED("TIME_LIMIT_EXCEEDED", "时间超限"),
	MEMORY_LIMIT_EXCEEDED("MEMORY_LIMIT_EXCEEDED", "内存超限"),
	OUTPUT_LIMIT_EXCEEDED("OUTPUT_LIMIT_EXCEEDED", "输出超限"),
	RUNTIME_ERROR("RUNTIME_ERROR", "运行时错误"),
	COMPILE_ERROR("COMPILE_ERROR", "编译错误"),
	SYSTEM_ERROR("SYSTEM_ERROR", "系统错误"),
	PRESENTATION_ERROR("PRESENTATION_ERROR", "格式错误"),
	SECURITY_ERROR("SECURITY_ERROR", "安全错误");


	@EnumValue
	private final String status;
	private final String description;

	JudgeStatus(String status, String description) {
		this.status = status;
		this.description = description;
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static JudgeStatus fromString(String status) {
		for (JudgeStatus js : JudgeStatus.values()) {
			if (js.status.equalsIgnoreCase(status)) {
				return js;
			}
		}
		throw new IllegalArgumentException("未知的判题状态: " + status);
	}

	@JsonValue
	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return this.status;
	}
}
