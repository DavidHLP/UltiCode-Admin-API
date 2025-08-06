package com.david.template;

import java.io.IOException;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import com.github.dockerjava.api.DockerClient;

import lombok.extern.slf4j.Slf4j;

/**
 * 沙箱执行模板抽象类
 * 定义了代码执行的通用流程，具体实现由子类完成
 */
@Slf4j
public abstract class SandboxTemplate {

    protected final DockerClient dockerClient;
    protected final String workDir = "/tmp/oj-sandbox";

    public SandboxTemplate(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * 模板方法：执行代码判题的完整流程
     */
    public final JudgeResult execute(SandboxExecuteRequest request) {
        String containerId = null;
        String tempDir = null;

        try {
            // 内联executeInternal逻辑，因为只使用一次
            tempDir = setupEnvironment(request);
            String sourceFile = writeSourceCode(tempDir, request.getSourceCode(), request.getLanguage());
            writeMainWrapper(tempDir, request.getLanguage(), request.getMainWrapperTemplate());

            containerId = createContainer(tempDir, request);
            startContainer(containerId);

            // 编译代码
            JudgeResult compileResult = compileCode(containerId, sourceFile, request.getLanguage(), request);
            if (compileResult.getStatus() == JudgeStatus.COMPILE_ERROR) {
                return compileResult;
            }

            // 执行测试用例
            JudgeResult testCasesResult = executeTestCases(containerId, request, tempDir);

            // 结果封装与清理
            return packageAndCleanup(containerId, tempDir, testCasesResult);

        } catch (Exception e) {
            log.error("沙箱执行异常: submissionId={}", request.getSubmissionId(), e);
            // 内联createErrorResult逻辑，使用JDK17的模式匹配
            return JudgeResult.builder()
                .submissionId(request.getSubmissionId())
                .status(JudgeStatus.SYSTEM_ERROR)
                .score(0)
                .timeUsed(0)
                .memoryUsed(0)
                .errorMessage("系统错误: " + e.getMessage())
                .build();
        } finally {
            // 内联cleanupResources逻辑，因为只使用一次
            if (containerId != null) {
                cleanupContainer(containerId);
            }
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    // 抽象方法定义
    protected abstract String setupEnvironment(SandboxExecuteRequest request) throws IOException;
    protected abstract String writeSourceCode(String tempDir, String sourceCode, LanguageType language) throws IOException;
    protected abstract void writeMainWrapper(String tempDir, LanguageType language, String mainWrapperTemplate) throws IOException;
    protected abstract String createContainer(String tempDir, SandboxExecuteRequest request);
    protected abstract void startContainer(String containerId);
    protected abstract JudgeResult compileCode(String containerId, String sourceFile, LanguageType language, SandboxExecuteRequest request);
    protected abstract JudgeResult executeTestCases(String containerId, SandboxExecuteRequest request, String tempDir);
    protected abstract JudgeResult packageAndCleanup(String containerId, String tempDir, JudgeResult testCasesResult);
    protected abstract void cleanupContainer(String containerId);
    protected abstract void cleanupTempDirectory(String tempDir);

    // 保留createSuccessResult因为被多次使用
    protected JudgeResult createSuccessResult() {
        return JudgeResult.builder()
            .status(JudgeStatus.ACCEPTED)
            .build();
    }
}