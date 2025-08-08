package com.david.strategy.utils;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * Docker环境配置
 */
@Data
@Builder
public class DockerEnvironmentConfig {
	private String imageName;              // Docker镜像名
	private String workingDirectory;       // 工作目录
	private Map<String, String> environmentVars; // 环境变量
	private List<String> requiredPackages; // 需要安装的包
	private String initCommand;            // 初始化命令
	private long memoryLimitBytes;         // 内存限制
	private int cpuLimit;                  // CPU限制
	private List<VolumeMount> volumeMounts; // 卷挂载
}