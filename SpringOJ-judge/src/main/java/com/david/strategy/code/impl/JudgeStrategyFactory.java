package com.david.strategy.code.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.david.judge.enums.LanguageType;
import com.david.strategy.code.JudgeStrategy;

/**
 * 简化的判题策略工厂
 */
@Component
public class JudgeStrategyFactory {

	private final Map<LanguageType, JudgeStrategy> strategyMap;

	public JudgeStrategyFactory(List<JudgeStrategy> strategies) {
		this.strategyMap = strategies.stream()
			.collect(Collectors.toMap(JudgeStrategy::getSupportedLanguage, Function.identity()));
	}

	public JudgeStrategy getStrategy(LanguageType language) {
		JudgeStrategy strategy = strategyMap.get(language);
		if (strategy == null) {
			throw new RuntimeException("不支持的语言类型: " + language);
		}
		return strategy;
	}

	public boolean isLanguageSupported(LanguageType language) {
		return strategyMap.containsKey(language);
	}
}