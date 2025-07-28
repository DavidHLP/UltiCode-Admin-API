package com.david.sandbox.dto;

import com.david.judge.enums.JudgeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseResult {
	/** 测试点ID */
	private Long testCaseId;

	/** 测试点状态 */
	private JudgeStatus status;

	/** 运行时间(ms) */
	private Integer timeUsed;

	/** 内存使用(KB) */
	private Integer memoryUsed;

	/** 得分 */
	private Integer score;

	/** 错误信息 */
	private String errorMessage;
}
