package com.david.service.impl;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import com.david.service.IDockerExecuteService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Docker执行服务实现类
 */
@Slf4j
@Service
public class DockerExecuteServiceImpl implements IDockerExecuteService {

    private final DockerClient dockerClient;
    private final String workDir = "/tmp/oj-sandbox";

    /**
     * 构造函数 - 使用依赖注入获取DockerClient
     * @param dockerClient Docker客户端实例
     */
    public DockerExecuteServiceImpl(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        // 确保工作目录存在
        new File(workDir).mkdirs();
        log.info("DockerExecuteService初始化完成，工作目录: {}", workDir);
    }

    @Override
    public JudgeResult executeCode(SandboxExecuteRequest request) {
        String containerId = null;
        try {
            // 创建临时目录
            String tempDir = createTempDirectory(request.getSubmissionId());
            
            // 写入源代码文件
            String sourceFile = writeSourceCode(tempDir, request.getSourceCode(), request.getLanguage());
            
            // 创建Docker容器
            containerId = createContainer(tempDir, request.getLanguage());
            
            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            
            // 编译代码
            JudgeResult compileResult = compileCode(containerId, sourceFile, request.getLanguage());
            if (compileResult.getStatus() == JudgeStatus.COMPILE_ERROR) {
                return compileResult;
            }
            
            // 执行测试用例
            return runTestCases(containerId, request, tempDir);
            
        } catch (Exception e) {
            log.error("Docker执行异常: submissionId={}", request.getSubmissionId(), e);
            return createErrorResult(JudgeStatus.SYSTEM_ERROR, "系统错误: " + e.getMessage());
        } finally {
            // 清理容器
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    log.warn("清理容器失败: {}", containerId, e);
                }
            }
        }
    }

    private String createTempDirectory(Long submissionId) throws IOException {
        String tempDir = workDir + "/submission_" + submissionId + "_" + System.currentTimeMillis();
        Files.createDirectories(Paths.get(tempDir));
        return tempDir;
    }

    private String writeSourceCode(String tempDir, String sourceCode, LanguageType language) throws IOException {
        String fileName = "Main" + language.getSuffix();
        String filePath = tempDir + "/" + fileName;
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sourceCode);
        }
        
        return fileName;
    }

    private String createContainer(String tempDir, LanguageType language) {
        String imageName = getDockerImage(language);
        
        Volume volume = new Volume("/code");
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(tempDir, volume))
                .withMemory(128 * 1024 * 1024L) // 128MB内存限制
                .withCpuQuota(50000L); // CPU限制
        
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)
                .withWorkingDir("/code")
                .withCmd("sleep", "300") // 保持容器运行5分钟
                .exec();
        
        return container.getId();
    }

    private String getDockerImage(LanguageType language) {
        return switch (language) {
            case JAVA -> "openjdk:17-alpine";
            case CPP, C -> "gcc:latest";
            case PYTHON3 -> "python:3.9-alpine";
            case PYTHON2 -> "python:2.7-alpine";
            default -> "ubuntu:20.04";
        };
    }

    private JudgeResult compileCode(String containerId, String sourceFile, LanguageType language) {
        try {
            String[] compileCmd = getCompileCommand(sourceFile, language);
            if (compileCmd == null) {
                // 解释型语言不需要编译
                return createSuccessResult();
            }
            
            ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(compileCmd)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            
            // 执行编译命令并获取结果
            // 这里需要实现具体的执行逻辑和结果解析
            
            return createSuccessResult();
        } catch (Exception e) {
            log.error("编译失败", e);
            return createErrorResult(JudgeStatus.COMPILE_ERROR, "编译错误: " + e.getMessage());
        }
    }

    private String[] getCompileCommand(String sourceFile, LanguageType language) {
        return switch (language) {
            case JAVA -> new String[]{"javac", sourceFile};
            case CPP -> new String[]{"g++", "-o", "main", sourceFile};
            case C -> new String[]{"gcc", "-o", "main", sourceFile};
            case PYTHON3, PYTHON2 -> null; // 解释型语言
            default -> null;
        };
    }

    private JudgeResult runTestCases(String containerId, SandboxExecuteRequest request, String tempDir) {
        JudgeResult result = new JudgeResult();
        result.setStatus(JudgeStatus.ACCEPTED);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        
        List<JudgeResult.TestCaseResult> testCaseResults = new ArrayList<>();
        
        for (int i = 0; i < request.getInputs().size(); i++) {
            String input = request.getInputs().get(i);
            String expectedOutput = request.getExpectedOutputs().get(i);
            
            JudgeResult.TestCaseResult testResult = runSingleTestCase(
                    containerId, input, expectedOutput, request, i + 1);
            
            testCaseResults.add(testResult);
            
            // 累计时间和内存
            result.setTimeUsed(result.getTimeUsed() + testResult.getTimeUsed());
            result.setMemoryUsed(Math.max(result.getMemoryUsed(), testResult.getMemoryUsed()));
            
            // 如果有测试点失败，更新总状态
            if (testResult.getStatus() != JudgeStatus.ACCEPTED) {
                result.setStatus(testResult.getStatus());
            } else {
                result.setScore(result.getScore() + testResult.getScore());
            }
        }
        
        result.setTestCaseResults(testCaseResults);
        return result;
    }

    private JudgeResult.TestCaseResult runSingleTestCase(String containerId, String input, 
            String expectedOutput, SandboxExecuteRequest request, int testCaseId) {
        
        JudgeResult.TestCaseResult result = new JudgeResult.TestCaseResult();
        result.setTestCaseId((long) testCaseId);
        result.setStatus(JudgeStatus.ACCEPTED);
        result.setTimeUsed(100); // 模拟执行时间
        result.setMemoryUsed(1024); // 模拟内存使用
        result.setScore(10); // 每个测试点10分
        
        try {
            // 这里需要实现具体的测试用例执行逻辑
            // 1. 将输入写入文件
            // 2. 执行程序
            // 3. 获取输出
            // 4. 比较输出结果
            // 5. 检查时间和内存限制
            
            log.info("执行测试用例 {}: submissionId={}", testCaseId, request.getSubmissionId());
            
        } catch (Exception e) {
            log.error("测试用例执行失败: testCase={}", testCaseId, e);
            result.setStatus(JudgeStatus.RUNTIME_ERROR);
            result.setScore(0);
            result.setErrorMessage("运行时错误: " + e.getMessage());
        }
        
        return result;
    }

    private JudgeResult createSuccessResult() {
        JudgeResult result = new JudgeResult();
        result.setStatus(JudgeStatus.ACCEPTED);
        return result;
    }

    private JudgeResult createErrorResult(JudgeStatus status, String errorMessage) {
        JudgeResult result = new JudgeResult();
        result.setStatus(status);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
