package com.david.judge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 判题状态枚举
 */
@Getter
public enum JudgeStatus {
	PENDING("Pending", "等待判题"),
	JUDGING("Judging", "判题中"),
	ACCEPTED("Accepted", "答案正确"),
	CONTINUE("Continue", "继续判题"),
	WRONG_ANSWER("Wrong Answer", "答案错误"),
	TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "时间超限"),
	MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "内存超限"),
	OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出超限"),
	RUNTIME_ERROR("Runtime Error", "运行时错误"),
	COMPILE_ERROR("Compile Error", "编译错误"),
	SYSTEM_ERROR("System Error", "系统错误"),
	PRESENTATION_ERROR("Presentation Error", "格式错误"),
	SECURITY_ERROR("Security Error", "安全错误");


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
