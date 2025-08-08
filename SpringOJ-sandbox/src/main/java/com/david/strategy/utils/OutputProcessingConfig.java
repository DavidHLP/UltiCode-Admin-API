package com.david.strategy.utils;

import java.util.function.Function;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;

/**
 * 输出处理配置
 */
@Data
@Builder
public class OutputProcessingConfig {
	private boolean trimWhitespace;        // 是否去除空白字符
	private boolean ignoreCase;            // 是否忽略大小写
	private String outputEncoding;         // 输出编码
	private Pattern errorPattern;          // 错误匹配模式
	private Function<String, String> outputNormalizer; // 输出标准化函数
}