package com.david.template.java;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.dto.TestCaseResult;
import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import com.david.template.SandboxTemplate;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.*;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Java 语言 Docker ACM 模式沙箱实现
 */
@Slf4j
@Component
public class JavaDockerAcmSandbox extends SandboxTemplate {

    private static final SandboxConfig CONFIG = SandboxConfig.DEFAULT;
    private final String memScriptHostPath;

    public JavaDockerAcmSandbox(DockerClient dockerClient) {
        super(dockerClient);
        this.memScriptHostPath = initializeMemScriptPath();
    }

    /**
     * 初始化内存脚本路径
     */
    private String initializeMemScriptPath() {
        try {
            var resource = this.getClass().getClassLoader().getResource("script/mem.sh");
            if (resource == null) {
                throw new IllegalStateException("无法找到内存监控脚本: script/mem.sh");
            }
            String path = resource.getPath();
            log.info("内存监控脚本路径: {}", path);
            return path;
        } catch (Exception e) {
            throw new RuntimeException("无法初始化内存监控脚本路径", e);
        }
    }

    @Override
    protected String setupEnvironment(SandboxExecuteRequest request) throws IOException {
        var tempDir = String.format("%s/submission_%d_%d",
                workDir, request.getSubmissionId(), System.currentTimeMillis());
        Files.createDirectories(Path.of(tempDir));
        log.info("创建临时目录: {}", tempDir);
        return tempDir;
    }

    @Override
    protected String writeSourceCode(String tempDir, String sourceCode, LanguageType language) throws IOException {
        var solutionFileName = "Solution" + language.getSuffix();
        var solutionFilePath = Path.of(tempDir, solutionFileName);

        var finalSourceCode = CONFIG.defaultImports() + sourceCode;

        Files.writeString(solutionFilePath, finalSourceCode, StandardCharsets.UTF_8);
        log.info("写入用户源代码文件: {}", solutionFilePath);
        return solutionFileName;
    }

    @Override
    protected void writeMainWrapper(String tempDir, LanguageType language, String mainWrapperTemplate)
            throws IOException {
        if (language == LanguageType.JAVA) {
            var mainFilePath = Path.of(tempDir, "Main" + language.getSuffix());
            Files.writeString(mainFilePath, mainWrapperTemplate, StandardCharsets.UTF_8);
            log.info("写入Main包装文件: {}", mainFilePath);
        }
    }

    @Override
    protected String createContainer(String tempDir, SandboxExecuteRequest request) {
        var imageName = getDockerImage(request.getLanguage());
        var memoryLimitBytes = request.getMemoryLimit() * 1024 * 1024L;

        var hostConfig = HostConfig.newHostConfig()
                .withNetworkMode("none")
                .withPidsLimit(CONFIG.pidLimit())
                .withCapDrop(Capability.ALL)
                .withMemory(memoryLimitBytes)
                .withMemorySwap(0L)
                .withCpuCount(CONFIG.cpuCount())
                .withBinds(
                        new Bind(tempDir, new Volume("/app")),
                        new Bind(memScriptHostPath, new Volume(CONFIG.memScriptContainerPath()))
                );

        var container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)
                .withWorkingDir("/app")
                .withTty(true)
                .withUser("0:0")
                .withCmd("sleep", "300")
                .exec();

        log.info("创建Docker容器: {}", container.getId());
        return container.getId();
    }

    @Override
    protected void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
        log.info("启动Docker容器: {}", containerId);

        // 设置目录权限
        setDirectoryPermissions(containerId);
    }

    /**
     * 设置容器内目录权限
     */
    private void setDirectoryPermissions(String containerId) {
        try {
            var chmodCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("chmod", "755", "/app")
                    .exec();
            dockerClient.execStartCmd(chmodCmd.getId())
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion();
            log.info("设置/app目录权限成功");
        } catch (Exception e) {
            log.warn("设置目录权限失败，但继续执行: {}", e.getMessage());
        }
    }

    @Override
    protected JudgeResult compileCode(String containerId, String sourceFile, LanguageType language,
                                      SandboxExecuteRequest request) {
        var compileCommand = getCompileCommand(sourceFile, language);

        if (compileCommand.length == 0) {
            return createSuccessResult();
        }

        return executeCompilation(containerId, compileCommand, request);
    }

    /**
     * 执行编译过程
     */
    private JudgeResult executeCompilation(String containerId, String[] compileCommand,
                                           SandboxExecuteRequest request) {
        try {
            var execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(compileCommand)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            var compileOutput = captureExecutionOutput(execCreateCmd.getId());

            if (!compileOutput.stderr().trim().isEmpty()) {
                return JudgeResult.builder()
                        .submissionId(request.getSubmissionId())
                        .status(JudgeStatus.COMPILE_ERROR)
                        .compileInfo(compileOutput.stderr().trim())
                        .build();
            }

            log.info("编译成功");
            return createSuccessResult();

        } catch (Exception e) {
            log.error("编译失败", e);
            return JudgeResult.builder()
                    .submissionId(request.getSubmissionId())
                    .status(JudgeStatus.SYSTEM_ERROR)
                    .errorMessage("编译错误: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 捕获命令执行输出
     */
    private ExecutionResult captureExecutionOutput(String execId) throws InterruptedException {
        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        var startTime = System.nanoTime();

        dockerClient.execStartCmd(execId).exec(new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                try {
                    switch (frame.getStreamType()) {
                        case STDOUT -> stdout.write(frame.getPayload());
                        case STDERR -> stderr.write(frame.getPayload());
                        default -> { /* ignore */ }
                    }
                } catch (IOException e) {
                    log.error("捕获输出异常", e);
                }
            }
        }).awaitCompletion();

        var endTime = System.nanoTime();
        var timeUsed = (endTime - startTime) / 1_000_000L;

        return new ExecutionResult(
                stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8),
                timeUsed,
                true
        );
    }

    @Override
    protected JudgeResult executeTestCases(String containerId, SandboxExecuteRequest request, String tempDir) {
        var resultBuilder = JudgeResult.builder()
                .submissionId(request.getSubmissionId())
                .status(JudgeStatus.ACCEPTED)
                .score(0)
                .timeUsed(0)
                .memoryUsed(0);

        var testCaseResults = new ArrayList<TestCaseResult>();
        var inputs = request.getInputs();
        var expectedOutputs = request.getExpectedOutputs();

        for (int i = 0; i < inputs.size(); i++) {
            var testResult = runSingleTestCase(
                    containerId,
                    inputs.get(i),
                    expectedOutputs.get(i),
                    request,
                    i + 1
            );

            testCaseResults.add(testResult);

            // 创建新的Builder实例来更新结果
            var currentResult = resultBuilder.build();
            resultBuilder = JudgeResult.builder()
                    .submissionId(currentResult.getSubmissionId())
                    .status(testResult.getStatus() != JudgeStatus.ACCEPTED ?
                            testResult.getStatus() : currentResult.getStatus())
                    .score(testResult.getStatus() == JudgeStatus.ACCEPTED ?
                            currentResult.getScore() + testResult.getScore() : currentResult.getScore())
                    .timeUsed(currentResult.getTimeUsed() + testResult.getTimeUsed())
                    .memoryUsed(Math.max(currentResult.getMemoryUsed(), testResult.getMemoryUsed()));
        }

        return resultBuilder.testCaseResults(testCaseResults).build();
    }

    /**
     * 运行单个测试用例
     */
    private TestCaseResult runSingleTestCase(String containerId, String input, String expectedOutput,
                                             SandboxExecuteRequest request, int testCaseId) {
        var resultBuilder = TestCaseResult.builder()
                .testCaseId((long) testCaseId)
                .status(JudgeStatus.ACCEPTED)
                .score(0)
                .memoryUsed(0);

        try {
            var memoryMonitor = startMemoryMonitoring(containerId);
            var executionResult = executeProgram(containerId, input, request);
            var maxMemory = stopMemoryMonitoring(memoryMonitor);

            resultBuilder.timeUsed((int) executionResult.timeUsed())
                    .memoryUsed((int) (maxMemory / 1024)); // bytes to KB

            if (!executionResult.completed()) {
                return resultBuilder
                        .status(JudgeStatus.TIME_LIMIT_EXCEEDED)
                        .errorMessage("时间超限")
                        .build();
            }

            if (!executionResult.stderr().trim().isEmpty()) {
                return resultBuilder
                        .status(JudgeStatus.RUNTIME_ERROR)
                        .errorMessage("运行时错误: " + executionResult.stderr())
                        .build();
            }

            return compareOutput(resultBuilder, executionResult.stdout(), expectedOutput);

        } catch (Exception e) {
            log.error("测试用例执行失败: testCase={}", testCaseId, e);
            return resultBuilder
                    .status(JudgeStatus.SYSTEM_ERROR)
                    .errorMessage("系统错误: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 启动内存监控
     */
    private MemoryMonitor startMemoryMonitoring(String containerId) throws IOException {
        var memResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", CONFIG.memScriptContainerPath(), "0.1")
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        var maxMemoryUsage = new AtomicLong(0L);
        var inputPipe = new PipedOutputStream();
        var input = new PipedInputStream(inputPipe);

        dockerClient.execStartCmd(memResponse.getId()).withStdIn(input)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        if (frame.getStreamType() == StreamType.STDOUT) {
                            var payload = new String(frame.getPayload()).trim();
                            if (!payload.isEmpty()) {
                                try {
                                    maxMemoryUsage.set(Math.max(maxMemoryUsage.get(), Long.parseLong(payload)));
                                } catch (NumberFormatException e) {
                                    log.debug("解析内存使用量失败: {}", payload);
                                }
                            }
                        }
                    }
                });

        return MemoryMonitor.builder()
                .outputPipe(inputPipe)
                .inputPipe(input)
                .maxMemoryUsage(maxMemoryUsage)
                .build();
    }

    /**
     * 停止内存监控
     */
    private long stopMemoryMonitoring(MemoryMonitor monitor) {
        try (var outputPipe = monitor.getOutputPipe(); var inputPipe = monitor.getInputPipe()) {
            outputPipe.write("q".getBytes());
            outputPipe.flush();
        } catch (IOException e) {
            log.warn("停止内存监控失败", e);
        }
        return monitor.getMaxMemoryUsage().get();
    }

    /**
     * 执行程序
     */
    private ExecutionResult executeProgram(String containerId, String input, SandboxExecuteRequest request)
            throws IOException, InterruptedException {
        var executeCmd = getExecuteCommand(request.getLanguage());
        var execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd(executeCmd)
                .withUser("nobody")
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        var output = new ByteArrayOutputStream();
        var error = new ByteArrayOutputStream();
        var startTime = System.nanoTime();

        var inputPipe = new PipedOutputStream();
        var programInput = new PipedInputStream(inputPipe);

        // 发送输入数据
        if (input != null && !input.isEmpty()) {
            inputPipe.write((input + "\n").getBytes());
            inputPipe.flush();
        }
        inputPipe.close();

        var callback = dockerClient.execStartCmd(execCreateCmd.getId())
                .withStdIn(programInput)
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        try {
                            switch (frame.getStreamType()) {
                                case STDOUT -> output.write(frame.getPayload());
                                case STDERR -> error.write(frame.getPayload());
                                default -> { /* ignore */ }
                            }
                        } catch (IOException e) {
                            log.error("捕获程序输出异常", e);
                        }
                    }
                });

        var completed = callback.awaitCompletion(request.getTimeLimit(), TimeUnit.MILLISECONDS);
        var endTime = System.nanoTime();
        var timeUsed = (endTime - startTime) / 1_000_000L;

        return new ExecutionResult(
                output.toString(StandardCharsets.UTF_8),
                error.toString(StandardCharsets.UTF_8),
                timeUsed,
                completed
        );
    }

    /**
     * 比较输出结果
     */
    private TestCaseResult compareOutput(TestCaseResult.TestCaseResultBuilder resultBuilder,
                                         String actualOutput, String expectedOutput) {
        var normalizedActual = normalizeOutput(actualOutput);
        var normalizedExpected = normalizeOutput(expectedOutput);

        if (Objects.equals(normalizedActual, normalizedExpected)) {
            return resultBuilder
                    .status(JudgeStatus.ACCEPTED)
                    .score(CONFIG.defaultScore())
                    .build();
        } else {
            var errorMessage = String.format("输出不匹配. 期望: %s, 实际: %s",
                    normalizedExpected, normalizedActual);
            log.error(errorMessage);
            return resultBuilder
                    .status(JudgeStatus.WRONG_ANSWER)
                    .errorMessage(errorMessage)
                    .build();
        }
    }

    /**
     * 标准化输出字符串
     */
    private String normalizeOutput(String output) {
        return output == null ? "" : output.replaceAll("[\\p{C}\\p{Z}]", "");
    }

    @Override
    protected JudgeResult packageAndCleanup(String containerId, String tempDir, JudgeResult testCasesResult) {
        log.info("判题完成，准备清理资源");
        return testCasesResult;
    }

    @Override
    protected void cleanupContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            log.info("清理容器成功: {}", containerId);
        } catch (Exception e) {
            log.warn("清理容器失败: {}", containerId, e);
        }
    }

    @Override
    protected void cleanupTempDirectory(String tempDir) {
        try {
            var path = Path.of(tempDir);
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                log.info("清理临时目录成功: {}", tempDir);
            }
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", tempDir, e);
        }
    }

    /**
     * 获取 Docker 镜像名称
     */
    private String getDockerImage(LanguageType language) {
        return switch (language) {
            case JAVA -> "openjdk:17-alpine";
            default -> "ubuntu:20.04";
        };
    }

    /**
     * 获取编译命令
     */
    private String[] getCompileCommand(String sourceFile, LanguageType language) {
        return switch (language) {
            case JAVA -> new String[]{"sh", "-c", "mkdir -p /tmp/classes && javac -d /tmp/classes /app/*.java"};
            default -> new String[]{};
        };
    }

    /**
     * 获取执行命令
     */
    private String[] getExecuteCommand(LanguageType language) {
        return switch (language) {
            case JAVA -> new String[]{"java", String.format("-Xmx%dm", CONFIG.defaultMemoryLimit()), "-cp", "/tmp/classes", "Main"};
            default -> new String[]{};
        };
    }

    /**
     * 创建成功地判题结果
     */
    protected JudgeResult createSuccessResult() {
        return JudgeResult.builder()
                .status(JudgeStatus.ACCEPTED)
                .build();
    }

    /**
     * 沙箱配置常量 - 使用 record (JDK 17 支持)
     */
    public record SandboxConfig(
            String memScriptContainerPath,
            String defaultImports,
            int defaultMemoryLimit,
            long pidLimit,
            long cpuCount,
            int defaultScore
    ) {
        public static final SandboxConfig DEFAULT = new SandboxConfig(
                "/script/mem.sh",
                """
                import java.util.*;
                import java.io.*;
                import java.math.*;
                
                """,
                128,
                64L,
                1L,
                10
        );
    }

    /**
     * 执行结果 - 使用 record
     */
    public record ExecutionResult(String stdout, String stderr, long timeUsed, boolean completed) {}

    /**
     * 内存监控器 - 使用 Lombok @Value 和 @Builder
     */
    @Value
    @Builder
    public static class MemoryMonitor {
        PipedOutputStream outputPipe;
        PipedInputStream inputPipe;
        AtomicLong maxMemoryUsage;
    }
}