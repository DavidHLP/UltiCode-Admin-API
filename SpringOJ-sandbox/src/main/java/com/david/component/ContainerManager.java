package com.david.component;

import org.springframework.stereotype.Component;

import com.david.constants.SandboxConstants;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.LanguageType;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 容器管理器 - 负责Docker容器的创建、启动和清理
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerManager {
    
    private final DockerClient dockerClient;
    
    /**
     * 创建Docker容器
     * 
     * @param tempDir 临时目录路径
     * @param request 沙箱执行请求
     * @param memScriptHostPath 内存监控脚本主机路径
     * @return 容器ID
     */
    public String createContainer(String tempDir, SandboxExecuteRequest request, String memScriptHostPath) {
        log.debug("开始创建容器: tempDir={}, language={}", tempDir, request.getLanguage());
        
        try {
            var imageName = getDockerImage(request.getLanguage());
            var memoryLimitBytes = request.getMemoryLimit() * 1024 * 1024L;
            
            var hostConfig = HostConfig.newHostConfig()
                    .withNetworkMode(SandboxConstants.DockerConfig.NETWORK_MODE)
                    .withPidsLimit(SandboxConstants.DefaultConfig.PID_LIMIT)
                    .withCapDrop(Capability.ALL)
                    .withMemory(memoryLimitBytes)
                    .withMemorySwap(0L)
                    .withCpuCount(SandboxConstants.DefaultConfig.CPU_COUNT)
                    .withBinds(
                            new Bind(tempDir, new Volume(SandboxConstants.DockerConfig.CONTAINER_APP_DIR)),
                            new Bind(memScriptHostPath, new Volume(SandboxConstants.DockerConfig.MEM_SCRIPT_CONTAINER_PATH))
                    );
            
            var container = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(SandboxConstants.DockerConfig.CONTAINER_APP_DIR)
                    .withCmd("tail", "-f", "/dev/null") // 保持容器运行
                    .exec();
            
            var containerId = container.getId();
            log.info(SandboxConstants.LogMessages.CONTAINER_CREATED, containerId);
            
            return containerId;
            
        } catch (Exception e) {
            log.error("容器创建失败: tempDir={}", tempDir, e);
            throw new RuntimeException(SandboxConstants.ErrorMessages.CONTAINER_CREATION_FAILED, e);
        }
    }
    
    /**
     * 启动容器
     * 
     * @param containerId 容器ID
     */
    public void startContainer(String containerId) {
        log.debug("启动容器: containerId={}", containerId);
        
        try {
            dockerClient.startContainerCmd(containerId).exec();
            log.info(SandboxConstants.LogMessages.CONTAINER_STARTED, containerId);
            
        } catch (Exception e) {
            log.error("容器启动失败: containerId={}", containerId, e);
            throw new RuntimeException(SandboxConstants.ErrorMessages.CONTAINER_START_FAILED, e);
        }
    }
    
    /**
     * 设置容器内目录权限
     * 
     * @param containerId 容器ID
     */
    public void setDirectoryPermissions(String containerId) {
        log.debug("设置容器目录权限: containerId={}", containerId);
        
        try {
            var execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("chmod", "-R", "755", SandboxConstants.DockerConfig.CONTAINER_APP_DIR)
                    .withAttachStdout(true)
                    .withAttachStderr(true);
            
            var execCreateResponse = execCreateCmd.exec();
            dockerClient.execStartCmd(execCreateResponse.getId()).exec(null);
            
            log.debug("容器目录权限设置完成: containerId={}", containerId);
            
        } catch (Exception e) {
            log.warn("设置容器目录权限失败: containerId={}", containerId, e);
            // 权限设置失败不应该中断整个流程
        }
    }
    
    /**
     * 清理容器
     * 
     * @param containerId 容器ID
     */
    public void cleanupContainer(String containerId) {
        if (containerId == null) {
            return;
        }
        
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            log.info(SandboxConstants.LogMessages.CONTAINER_CLEANED, containerId);
            
        } catch (Exception e) {
            log.warn(SandboxConstants.ErrorMessages.CONTAINER_CLEANUP_FAILED, containerId, e);
        }
    }
    
    /**
     * 获取Docker镜像名称
     */
    private String getDockerImage(LanguageType language) {
        return switch (language) {
            case JAVA -> SandboxConstants.DockerConfig.JAVA_IMAGE;
            default -> SandboxConstants.DockerConfig.UBUNTU_IMAGE;
        };
    }
}
