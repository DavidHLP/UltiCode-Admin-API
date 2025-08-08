package com.david.chain.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.david.chain.JudgeHandler;
import com.david.chain.utils.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.judge.enums.LanguageType;
import com.david.strategy.LanguageStrategy;
import com.david.strategy.LanguageStrategyFactory;
import com.david.strategy.utils.FileConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 环境初始化处理器 - 通用化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnvironmentSetupHandler extends JudgeHandler {

	private final LanguageStrategyFactory strategyFactory;

	@Override
	protected JudgeResult doHandle(JudgeContext context) {
		try {
			// 获取语言策略
			LanguageStrategy strategy = strategyFactory.getStrategy(context.getRequest().getLanguage());
			context.setLanguageStrategy(strategy);

			// 创建临时目录
			setupTempDirectory(context);

			// 写入源代码文件
			writeSourceCodeFile(context, strategy);

			// 写入包装文件（如果需要）
			writeWrapperFiles(context, strategy);

			log.info("环境初始化完成: language={}, tempDir={}",
					strategy.getLanguageType(), context.getTempDir());
			return continueResult();

		} catch (Exception e) {
			log.error("环境初始化失败: language={}", context.getRequest().getLanguage(), e);
			return errorResult(context.getRequest(), "环境初始化失败: " + e.getMessage());
		}
	}

	private void setupTempDirectory(JudgeContext context) throws IOException {
		String tempDir = String.format("/tmp/oj-sandbox/%s_%d_%d",
				context.getRequest().getLanguage().name().toLowerCase(),
				context.getRequest().getSubmissionId(),
				System.currentTimeMillis());
		Files.createDirectories(Path.of(tempDir));
		context.setTempDir(tempDir);
	}

	private void writeSourceCodeFile(JudgeContext context, LanguageStrategy strategy) throws IOException {
		FileConfig fileConfig = strategy.getFileConfig();
		String processedCode = strategy.preprocessSourceCode(context.getRequest().getSourceCode());

		Path sourceFile = Path.of(context.getTempDir(), fileConfig.getSourceFileName());
		Files.writeString(sourceFile, processedCode, StandardCharsets.UTF_8);

		context.setSourceFileName(fileConfig.getSourceFileName());
		log.debug("写入源代码文件: {}", sourceFile);
	}

	private void writeWrapperFiles(JudgeContext context, LanguageStrategy strategy) throws IOException {
		String wrapperTemplate = context.getRequest().getMainWrapperTemplate();
		if (wrapperTemplate != null && !wrapperTemplate.isEmpty()) {
			// 根据语言类型决定包装文件名
			String wrapperFileName = determineWrapperFileName(strategy.getLanguageType());
			Path wrapperFile = Path.of(context.getTempDir(), wrapperFileName);
			Files.writeString(wrapperFile, wrapperTemplate, StandardCharsets.UTF_8);
			log.debug("写入包装文件: {}", wrapperFile);
		}
	}

	private String determineWrapperFileName(LanguageType languageType) {
		return switch (languageType) {
			case JAVA -> "Main.java";
			case PYTHON -> "main.py";
			case CPP -> "main.cpp";
			case GO -> "main_wrapper.go";
			case JAVASCRIPT -> "main.js";
			default -> "main.txt";
		};
	}
}
