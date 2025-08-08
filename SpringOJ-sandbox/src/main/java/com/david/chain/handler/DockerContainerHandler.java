package com.david.chain.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.david.chain.JudgeHandler;
import com.david.chain.utils.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.strategy.LanguageStrategy;
import com.david.strategy.utils.DockerEnvironmentConfig;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;

import lombok.extern.slf4j.Slf4j;

/**
 * Docker容器处理器 - 通用化
 */
@Slf4j
@Component
public class DockerContainerHandler extends JudgeHandler {

	@Override
	protected JudgeResult doHandle(JudgeContext context) {
		try {
			LanguageStrategy strategy = context.getLanguageStrategy();
			DockerEnvironmentConfig dockerConfig = strategy.getDockerConfig();

			// 创建容器
			String containerId = createContainer(context, dockerConfig);
			context.setContainerId(containerId);

			// 启动容器
			startContainer(context, dockerConfig);

			// 安装必需的包（如果有）
			installRequiredPackages(context, dockerConfig);

			log.info("Docker容器准备完成: language={}, containerId={}",
					strategy.getLanguageType(), containerId);
			return continueResult();

		} catch (Exception e) {
			log.error("Docker容器创建失败: language={}",
					context.getLanguageStrategy().getLanguageType(), e);
			return errorResult(context.getRequest(), "容器创建失败: " + e.getMessage());
		}
	}

	private String createContainer(JudgeContext context, DockerEnvironmentConfig dockerConfig) {
		SandboxExecuteRequest request = context.getRequest();

		// 构建环境变量
		List<String> envVars = dockerConfig.getEnvironmentVars().entrySet().stream()
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.toList());

		// 构建主机配置
		HostConfig hostConfig = HostConfig.newHostConfig()
				.withNetworkMode("none")
				.withPidsLimit(64L)
				.withCapDrop(Capability.ALL)
				.withMemory(request.getMemoryLimit() * 1024 * 1024L)
				.withMemorySwap(0L)
				.withCpuCount(1L)
				.withBinds(new Bind(context.getTempDir(), new Volume(dockerConfig.getWorkingDirectory())));

		// 创建容器
		CreateContainerResponse container = context.getDockerClient()
				.createContainerCmd(dockerConfig.getImageName())
				.withHostConfig(hostConfig)
				.withWorkingDir(dockerConfig.getWorkingDirectory())
				.withEnv(envVars)
				.withTty(true)
				.withUser("0:0")
				.withCmd("sleep", "300")
				.exec();

		return container.getId();
	}

	private void startContainer(JudgeContext context, DockerEnvironmentConfig dockerConfig) {
		context.getDockerClient().startContainerCmd(context.getContainerId()).exec();

		// 设置目录权限
		setDirectoryPermissions(context, dockerConfig.getWorkingDirectory());

		// 执行初始化命令（如果有）
		if (dockerConfig.getInitCommand() != null) {
			executeInitCommand(context, dockerConfig.getInitCommand());
		}
	}

	private void installRequiredPackages(JudgeContext context, DockerEnvironmentConfig dockerConfig) {
		List<String> packages = dockerConfig.getRequiredPackages();
		if (packages != null && !packages.isEmpty()) {
			String installCommand = String.join(" ", packages);
			executeCommand(context, new String[]{"sh", "-c", installCommand});
		}
	}
}
