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
     * 
     * @param request 沙箱执行请求
     * @return 判题结果
     */
    public final JudgeResult execute(SandboxExecuteRequest request) {
        String containerId = null;
        String tempDir = null;
        try {
            // 1. 环境准备与隔离
            tempDir = setupEnvironment(request);
            String sourceFile = writeSourceCode(tempDir, request.getSourceCode(), request.getLanguage());
            writeMainWrapper(tempDir, request.getLanguage(), request.getMainWrapperTemplate());
            containerId = createContainer(tempDir, request);
            startContainer(containerId);

            // 2. 编译代码
            JudgeResult compileResult = compileCode(containerId, sourceFile, request.getLanguage(), request);
            if (compileResult.getStatus() == JudgeStatus.COMPILE_ERROR) {
                return compileResult;
            }

            // 3. 循环执行测试用例
            JudgeResult testCasesResult = executeTestCases(containerId, request, tempDir);

            // 4. 结果封装与清理
            return packageAndCleanup(containerId, tempDir, testCasesResult);

        } catch (Exception e) {
            log.error("沙箱执行异常: submissionId={}", request.getSubmissionId(), e);
            JudgeResult errorResult = createErrorResult(JudgeStatus.SYSTEM_ERROR, "系统错误: " + e.getMessage());
            errorResult.setSubmissionId(request.getSubmissionId());
            return errorResult;
        } finally {
            // 确保容器被清理
            if (containerId != null) {
                cleanupContainer(containerId);
            }
            // 确保临时目录被清理
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    /**
     * 抽象方法：环境准备与隔离
     * 
     * @param request 沙箱执行请求
     * @return 临时工作目录路径
     * @throws IOException IO异常
     */
    protected abstract String setupEnvironment(SandboxExecuteRequest request) throws IOException;

    /**
     * 抽象方法：写入源代码文件
     * 
     * @param tempDir    临时工作目录
     * @param sourceCode 源代码
     * @param language   编程语言
     * @return 源代码文件名
     * @throws IOException IO异常
     */
    protected abstract String writeSourceCode(String tempDir, String sourceCode,
            LanguageType language) throws IOException;

    /**
     * 抽象方法：写入主函数包装文件
     * 
     * @param tempDir 临时工作目录
     * @param language 编程语言
     * @throws IOException IO异常
     */
    protected abstract void writeMainWrapper(String tempDir, LanguageType language , String mainWrapperTemplate) throws IOException;

    /**
     * 抽象方法：创建Docker容器
     * 
     * @param tempDir 临时工作目录
     * @param request 沙箱执行请求
     * @return 容器ID
     */
    protected abstract String createContainer(String tempDir, SandboxExecuteRequest request);

    /**
     * 抽象方法：启动Docker容器
     * 
     * @param containerId 容器ID
     */
    protected abstract void startContainer(String containerId);

    /**
     * 抽象方法：编译代码
     * 
     * @param containerId 容器ID
     * @param sourceFile  源代码文件名
     * @param language    编程语言
     * @return 编译结果
     */
    protected abstract JudgeResult compileCode(String containerId, String sourceFile,
            LanguageType language, SandboxExecuteRequest request);

    /**
     * 抽象方法：执行测试用例
     * 
     * @param containerId 容器ID
     * @param request     沙箱执行请求
     * @param tempDir     临时工作目录
     * @return 测试用例执行结果
     */
    protected abstract JudgeResult executeTestCases(String containerId, SandboxExecuteRequest request, String tempDir);

    /**
     * 抽象方法：结果封装与清理
     * 
     * @param containerId     容器ID
     * @param tempDir         临时工作目录
     * @param testCasesResult 测试用例执行结果
     * @return 最终判题结果
     */
    protected abstract JudgeResult packageAndCleanup(String containerId, String tempDir, JudgeResult testCasesResult);

    /**
     * 抽象方法：清理Docker容器
     * 
     * @param containerId 容器ID
     */
    protected abstract void cleanupContainer(String containerId);

    /**
     * 抽象方法：清理临时目录
     * 
     * @param tempDir 临时目录路径
     */
    protected abstract void cleanupTempDirectory(String tempDir);

    /**
     * 辅助方法：创建错误结果
     * 
     * @param status       判题状态
     * @param errorMessage 错误信息
     * @return 判题结果
     */
    protected JudgeResult createErrorResult(JudgeStatus status, String errorMessage) {
        JudgeResult result = new JudgeResult();
        result.setStatus(status);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        result.setErrorMessage(errorMessage);
        return result;
    }

    /**
     * 辅助方法：创建成功结果
     * 
     * @return 判题结果
     */
    protected JudgeResult createSuccessResult() {
        JudgeResult result = new JudgeResult();
        result.setStatus(JudgeStatus.ACCEPTED);
        return result;
    }
}