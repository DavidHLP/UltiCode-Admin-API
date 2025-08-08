package com.david.strategy.impl;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.david.judge.enums.LanguageType;
import com.david.strategy.LanguageStrategy;
import com.david.strategy.utils.*;

/**
 * Java语言策略
 */
@Component
public class JavaLanguageStrategy implements LanguageStrategy {

	@Override
	public LanguageType getLanguageType() {
		return LanguageType.JAVA;
	}

	@Override
	public DockerEnvironmentConfig getDockerConfig() {
		return DockerEnvironmentConfig.builder()
				.imageName("openjdk:17-alpine")
				.workingDirectory("/app")
				.environmentVars(Map.of(
						"JAVA_OPTS", "-server",
						"LANG", "C.UTF-8"))
				.build();
	}

	@Override
	public FileConfig getFileConfig() {
		return FileConfig.builder()
				.sourceFileName("Solution.java")
				.executableName("Main")
				.supportedExtensions(List.of(".java"))
				.build();
	}

	@Override
	public String preprocessSourceCode(String originalCode) {
		return """
				import java.util.*;
				import java.io.*;
				import java.math.*;

				""" + originalCode;
	}

	@Override
	public CompilationConfig getCompilationConfig() {
		return CompilationConfig.builder()
				.needsCompilation(true)
				.compileCommand(new String[] { "sh", "-c",
						"mkdir -p /tmp/classes && javac -d /tmp/classes /app/*.java" })
				.compileTimeoutSeconds(30)
				.build();
	}

	@Override
	public ExecutionConfig getExecutionConfig() {
		return ExecutionConfig.builder()
				.executeCommand(new String[] { "java", "-cp", "/tmp/classes", "Main" })
				.runAsUser("nobody")
				.memoryLimitParam("-Xmx%dm")
				.build();
	}

	@Override
	public OutputProcessingConfig getOutputConfig() {
		return OutputProcessingConfig.builder()
				// 关闭全局空白折叠，避免将换行折叠为空格
				.trimWhitespace(false)
				.outputEncoding("UTF-8")
				.errorPattern(Pattern.compile("Exception|Error"))
				// 使用智能标准化器，消除 [0,1] vs [0, 1] 等格式性差异
				.outputNormalizer(SmartOutputNormalizer::normalize)
				.build();
	}
}
