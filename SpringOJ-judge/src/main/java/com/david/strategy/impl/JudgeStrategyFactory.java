package com.david.strategy.impl;

import org.springframework.stereotype.Component;

import com.david.judge.enums.LanguageType;
import com.david.strategy.JudgeStrategy;

import lombok.RequiredArgsConstructor;

/**
 * @author david
 * @since 2023/12/5
 */
@Component
@RequiredArgsConstructor
public class JudgeStrategyFactory {
	private final JavaJudgeStrategy javaJudgeStrategy;

	public JudgeStrategy getStrategy(LanguageType language) {
		JudgeStrategy judgeStrategy = switch (language) {
			case JAVA -> javaJudgeStrategy;
			default -> null;
		};
		if (judgeStrategy == null) {
			throw new RuntimeException("不支持的语言类型");
		}
		return judgeStrategy;
	}
}