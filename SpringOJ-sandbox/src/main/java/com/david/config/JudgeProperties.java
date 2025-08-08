package com.david.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 判题系统属性配置
 */
@ConfigurationProperties(prefix = "judge")
@Data
public class JudgeProperties {

	/**
	 * 工作目录
	 */
	private String workDir = "/tmp/oj-sandbox";

	/**
	 * 默认内存限制(MB)
	 */
	private int defaultMemoryLimit = 128;

	/**
	 * 默认时间限制(ms)
	 */
	private int defaultTimeLimit = 2000;

	/**
	 * 默认编译超时(s)
	 */
	private int defaultCompileTimeout = 30;

	/**
	 * 是否在第一个测试用例失败后停止
	 */
	private boolean stopOnFirstFailure = false;

	/**
	 * 支持的最大测试用例数量
	 */
	private int maxTestCases = 100;

	/**
	 * Docker相关配置
	 */
	private Docker docker = new Docker();

	@Data
	public static class Docker {
		private String registryUrl = "docker.io";
		private int pullTimeout = 300;
		private int containerTimeout = 300;
		private boolean autoCleanup = true;
	}
}
