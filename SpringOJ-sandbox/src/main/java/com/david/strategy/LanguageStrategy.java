package com.david.strategy;

import com.david.judge.enums.LanguageType;
import com.david.strategy.utils.*;

/**
 * 通用编程语言策略接口
 * 支持编译型、解释型、字节码型等各种语言
 */
public interface LanguageStrategy {

	/**
	 * 获取语言类型
	 */
	LanguageType getLanguageType();

	/**
	 * 获取Docker运行环境配置
	 */
	DockerEnvironmentConfig getDockerConfig();

	/**
	 * 获取文件配置（源文件名、可执行文件名等）
	 */
	FileConfig getFileConfig();

	/**
	 * 预处理源代码（添加导入、包装类等）
	 */
	String preprocessSourceCode(String originalCode);

	/**
	 * 获取编译阶段配置
	 */
	CompilationConfig getCompilationConfig();

	/**
	 * 获取执行阶段配置
	 */
	ExecutionConfig getExecutionConfig();

	/**
	 * 获取输出处理配置
	 */
	OutputProcessingConfig getOutputConfig();
}
