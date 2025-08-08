package com.david.strategy.utils;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * 执行配置
 */
@Data
@Builder
public class ExecutionConfig {
	private String[] executeCommand;       // 执行命令
	private String executeWorkDir;         // 执行工作目录
	private String runAsUser;              // 运行用户
	private boolean needsInputFile;        // 是否需要输入文件
	private boolean needsOutputFile;       // 是否需要输出文件
	private String memoryLimitParam;       // 内存限制参数
	private Map<String, String> runtimeEnvVars; // 运行时环境变量
}