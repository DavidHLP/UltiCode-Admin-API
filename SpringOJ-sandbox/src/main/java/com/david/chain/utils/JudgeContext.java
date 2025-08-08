package com.david.chain.utils;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.strategy.LanguageStrategy;
import com.github.dockerjava.api.DockerClient;

import lombok.Builder;
import lombok.Data;

/**
 * 判题上下文
 */
@Data
@Builder
public class JudgeContext {
	private SandboxExecuteRequest request;
	private String tempDir;
	private String containerId;
	private String sourceFileName;
	private LanguageStrategy languageStrategy;
	private DockerClient dockerClient;
	private JudgeResult currentResult;

	// 添加一些工具方法
	public void updateResult(JudgeResult result) {
		this.currentResult = result;
	}
}
