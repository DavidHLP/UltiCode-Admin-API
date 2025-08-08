package com.david.strategy.utils;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * 编译配置
 */
@Data
@Builder
public class CompilationConfig {
	private boolean needsCompilation;      // 是否需要编译
	private String[] compileCommand;       // 编译命令
	private String compileWorkDir;         // 编译工作目录
	private int compileTimeoutSeconds;     // 编译超时时间
	private Map<String, String> compileEnvVars; // 编译环境变量
	private String compilerVersion;        // 编译器版本
}
