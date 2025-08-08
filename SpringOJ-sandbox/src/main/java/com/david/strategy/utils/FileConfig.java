package com.david.strategy.utils;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 文件配置
 */
@Data
@Builder
public class FileConfig {
	private String sourceFileName;         // 源文件名
	private String compiledFileName;       // 编译后文件名（可选）
	private String executableName;         // 可执行文件名
	private String mainWrapperTemplate;    // Main包装模板
	private List<String> supportedExtensions; // 支持的文件扩展名
}
