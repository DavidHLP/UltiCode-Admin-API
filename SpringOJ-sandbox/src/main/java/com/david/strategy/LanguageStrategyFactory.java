package com.david.strategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.david.judge.enums.LanguageType;

import lombok.extern.slf4j.Slf4j;

/**
 * 语言策略工厂 - 支持动态注册
 */
@Slf4j
@Component
public class LanguageStrategyFactory {

	private final Map<LanguageType, LanguageStrategy> strategies = new ConcurrentHashMap<>();
	private final Map<String, LanguageType> extensionMapping = new ConcurrentHashMap<>();

	public LanguageStrategyFactory(List<LanguageStrategy> strategyList) {
		// 自动注册所有策略
		strategyList.forEach(this::registerStrategy);
	}

	public void registerStrategy(LanguageStrategy strategy) {
		LanguageType type = strategy.getLanguageType();
		strategies.put(type, strategy);

		// 注册文件扩展名映射
		List<String> extensions = strategy.getFileConfig().getSupportedExtensions();
		if (extensions != null) {
			extensions.forEach(ext -> extensionMapping.put(ext, type));
		}

		log.info("注册语言策略: {} -> {}", type, strategy.getClass().getSimpleName());
	}

	public LanguageStrategy getStrategy(LanguageType languageType) {
		LanguageStrategy strategy = strategies.get(languageType);
		if (strategy == null) {
			throw new UnsupportedOperationException("不支持的编程语言: " + languageType);
		}
		return strategy;
	}

	public LanguageStrategy getStrategyByExtension(String extension) {
		LanguageType type = extensionMapping.get(extension.toLowerCase());
		if (type == null) {
			throw new UnsupportedOperationException("不支持的文件扩展名: " + extension);
		}
		return getStrategy(type);
	}

	public Set<LanguageType> getSupportedLanguages() {
		return Collections.unmodifiableSet(strategies.keySet());
	}
}

