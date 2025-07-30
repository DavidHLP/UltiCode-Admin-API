package com.david.template.java;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import com.david.sandbox.dto.JudgeResult;
import com.david.sandbox.dto.SandboxExecuteRequest;
import com.david.sandbox.dto.TestCaseResult;
import com.david.template.SandboxTemplate;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Java 语言 Docker ACM 模式沙箱实现
 */
@Slf4j
@Component
public class JavaDockerAcmSandbox extends SandboxTemplate {
    private static final String MEM_SCRIPT_CONTAINER_PATH = "/script/mem.sh";
    private final String memScriptHostPath;

    public JavaDockerAcmSandbox(DockerClient dockerClient) {
        super(dockerClient);
        // 动态获取 resources/script/mem.sh 的绝对路径
        try {
            String resourcePath = this.getClass().getClassLoader().getResource("script/mem.sh").getPath();
            this.memScriptHostPath = resourcePath;
            log.info("内存监控脚本路径: {}", this.memScriptHostPath);
        } catch (Exception e) {
            throw new RuntimeException("无法找到内存监控脚本: script/mem.sh", e);
        }
    }

    @Override
    protected String setupEnvironment(SandboxExecuteRequest request) throws IOException {
        String tempDir = workDir + "/submission_" + request.getSubmissionId() + "_" + System.currentTimeMillis();
        Files.createDirectories(Paths.get(tempDir));
        log.info("创建临时目录: {}", tempDir);
        return tempDir;
    }

    @Override
    protected String writeSourceCode(String tempDir, String sourceCode, LanguageType language) throws IOException {
        String fileName = "Main" + language.getSuffix();
        String filePath = tempDir + File.separator + fileName;

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sourceCode);
        }
        log.info("写入源代码文件: {}", filePath);
        return fileName;
    }

    @Override
    protected String createContainer(String tempDir, SandboxExecuteRequest request) {
        String imageName = getDockerImage(request.getLanguage());

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withNetworkMode("none") // 禁止网络访问
                .withPidsLimit(64L) // 限制进程数
                .withCapDrop(Capability.ALL) // 限制权限
                .withMemory(request.getMemoryLimit() * 1024 * 1024L) // 内存限制 (MB to bytes)
                .withMemorySwap(0L) // 交换区0MB
                .withCpuCount(1L); // CPU限制为单核

        // 绑定代码目录和内存监控脚本
        hostConfig.setBinds(new Bind(tempDir, new Volume("/app")),
                new Bind(memScriptHostPath, new Volume(MEM_SCRIPT_CONTAINER_PATH)));

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)
                .withWorkingDir("/app") // 设置工作目录为 /app
                .withTty(true) // 分配一个伪终端
                .withUser("0:0") // 使用root用户确保有写权限
                .withCmd("sleep", "300") // 保持容器运行5分钟
                .exec();
        log.info("创建Docker容器: {}", container.getId());
        return container.getId();
    }

    @Override
    protected void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
        log.info("启动Docker容器: {}", containerId);

        // 确保/app目录有正确的权限
        try {
            ExecCreateCmdResponse chmodCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("chmod", "755", "/app")
                    .exec();
            dockerClient.execStartCmd(chmodCmd.getId()).exec(new ResultCallback.Adapter<>()).awaitCompletion();
            log.info("设置/app目录权限成功");
        } catch (Exception e) {
            log.warn("设置目录权限失败，但继续执行: {}", e.getMessage());
        }
    }

    @Override
    protected JudgeResult compileCode(String containerId, String sourceFile, LanguageType language, SandboxExecuteRequest request) {
        JudgeResult compileResult = new JudgeResult();
        compileResult.setSubmissionId(request.getSubmissionId()); // 设置 submissionId
        try {
            String[] compileCmd = getCompileCommand(sourceFile, language);
            if (compileCmd.length == 0) {
                return createSuccessResult();
            }

            ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(compileCmd)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayOutputStream compileStdout = new ByteArrayOutputStream();
            ByteArrayOutputStream compileStderr = new ByteArrayOutputStream();

            dockerClient.execStartCmd(execCreateCmd.getId())
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            try {
                                if (frame.getStreamType() == StreamType.STDOUT) {
                                    compileStdout.write(frame.getPayload());
                                } else if (frame.getStreamType() == StreamType.STDERR) {
                                    compileStderr.write(frame.getPayload());
                                }
                            } catch (IOException e) {
                                log.error("捕获编译输出异常", e);
                            }
                        }
                    }).awaitCompletion();

            String stderr = compileStderr.toString("UTF-8").trim();
            if (stderr.length() > 0) {
                compileResult.setStatus(JudgeStatus.COMPILE_ERROR);
                compileResult.setCompileInfo(stderr);
                log.warn("编译错误: submissionId={}, info={}", request.getSubmissionId(), compileResult.getCompileInfo());
                return compileResult;
            }
            log.info("编译成功");
            return createSuccessResult();
        } catch (Exception e) {
            log.error("编译失败", e);
            compileResult.setStatus(JudgeStatus.SYSTEM_ERROR);
            compileResult.setErrorMessage("编译错误: " + e.getMessage());
            return compileResult;
        }
    }

    @Override
    protected JudgeResult executeTestCases(String containerId, SandboxExecuteRequest request, String tempDir) {
        JudgeResult result = new JudgeResult();
        result.setStatus(JudgeStatus.ACCEPTED);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        result.setSubmissionId(request.getSubmissionId());

        List<TestCaseResult> testCaseResults = new ArrayList<>();

        for (int i = 0; i < request.getInputs().size(); i++) {
            String input = request.getInputs().get(i);
            String expectedOutput = request.getExpectedOutputs().get(i);

            TestCaseResult testResult = runSingleTestCase(
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
            Files.walk(Paths.get(tempDir))
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(File::delete);
            log.info("清理临时目录成功: {}", tempDir);
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", tempDir, e);
        }
    }

    private String getDockerImage(LanguageType language) {
        return switch (language) {
            case JAVA -> "openjdk:17-alpine";
            default -> "ubuntu:20.04"; // Fallback for other languages not yet implemented
        };
    }

    private String[] getCompileCommand(String sourceFile, LanguageType language) {
        return switch (language) {
            case JAVA ->
                new String[] { "sh", "-c", "mkdir -p /tmp/classes && javac -d /tmp/classes /app/" + sourceFile };
            default -> new String[] {}; // 解释型语言或默认情况
        };
    }

    private TestCaseResult runSingleTestCase(String containerId, String input,
                                             String expectedOutput, SandboxExecuteRequest request, int testCaseId) {

        TestCaseResult result = new TestCaseResult();
        result.setTestCaseId((long) testCaseId);
        result.setStatus(JudgeStatus.ACCEPTED);
        result.setScore(0); // Default score
        result.setMemoryUsed(0);

        try {
            // 1. 启动内存监控
            AtomicLong maxMemoryUsage = new AtomicLong(0L);
            ExecCreateCmdResponse memResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("/bin/sh", MEM_SCRIPT_CONTAINER_PATH, "0.1") // 运行内存监控脚本
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            String memExecId = memResponse.getId();

            PipedOutputStream memInputPipe = new PipedOutputStream();
            PipedInputStream memInput = new PipedInputStream(memInputPipe);
            ByteArrayOutputStream memStdout = new ByteArrayOutputStream();
            ByteArrayOutputStream memStderr = new ByteArrayOutputStream();

            dockerClient.execStartCmd(memExecId).withStdIn(memInput).exec(new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    try {
                        if (frame.getStreamType() == StreamType.STDOUT) {
                            String payload = new String(frame.getPayload()).trim();
                            if (!payload.isEmpty()) {
                                maxMemoryUsage.set(Math.max(maxMemoryUsage.get(), Long.parseLong(payload)));
                                memStdout.write(frame.getPayload());
                            }
                        } else if (frame.getStreamType() == StreamType.STDERR) {
                            memStderr.write(frame.getPayload());
                        }
                    } catch (IOException | NumberFormatException e) {
                        log.error("捕获内存输出异常", e);
                    }
                }
            });

            // 2. 执行程序
            String[] executeCmd = getExecuteCommand(request.getLanguage());
            ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(executeCmd)
                    .withUser("nobody") // 以 nobody 用户运行
                    .withAttachStdin(true) // 需要attach stdin来传递输入
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            long startTime = System.nanoTime();

            PipedOutputStream runInputPipe = new PipedOutputStream();
            PipedInputStream runInput = new PipedInputStream(runInputPipe);

            // 向程序传递输入数据
            if (input != null) {
                runInputPipe.write(input.getBytes());
                runInputPipe.write("\n".getBytes()); // 添加换行符
                runInputPipe.flush();
                runInputPipe.close();
            }

            // Start the execution and capture output
            ResultCallback.Adapter<Frame> callback = dockerClient.execStartCmd(execCreateCmd.getId())
                    .withStdIn(runInput)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            try {
                                if (frame.getStreamType() == StreamType.STDOUT) {
                                    output.write(frame.getPayload());
                                } else if (frame.getStreamType() == StreamType.STDERR) {
                                    error.write(frame.getPayload());
                                }
                            } catch (IOException e) {
                                log.error("捕获程序输出异常", e);
                            }
                        }
                    });

            // Wait for completion with timeout
            boolean completed = callback.awaitCompletion(request.getTimeLimit(), TimeUnit.MILLISECONDS);
            long endTime = System.nanoTime();
            long timeUsed = (endTime - startTime) / 1_000_000; // Convert nanoseconds to milliseconds

            result.setTimeUsed((int) timeUsed);

            // 停止内存监控
            memInputPipe.write("q".getBytes());
            memInputPipe.flush();
            memInputPipe.close();
            memInput.close();
            memStdout.close();
            memStderr.close();

            result.setMemoryUsed((int) (maxMemoryUsage.get() / 1024)); // bytes to KB

            if (!completed) {
                // Time Limit Exceeded
                result.setStatus(JudgeStatus.TIME_LIMIT_EXCEEDED);
                result.setErrorMessage("时间超限");
                return result;
            }

            // Check for Runtime Error
            String stderr = error.toString("UTF-8").trim();
            if (stderr.length() > 0) {
                result.setStatus(JudgeStatus.RUNTIME_ERROR);
                result.setErrorMessage("运行时错误: " + stderr);
                return result;
            }

            // Compare output
            String actualOutput = output.toString("UTF-8").replaceAll("\r\n", "\n").trim();
            String expectedOutputTrimmed = expectedOutput.replaceAll("\r\n", "\n").trim();

            if (actualOutput.equals(expectedOutputTrimmed)) {
                result.setStatus(JudgeStatus.ACCEPTED);
                result.setScore(10); // Assuming 10 points per test case
            } else {
                result.setStatus(JudgeStatus.WRONG_ANSWER);
                result.setErrorMessage("输出不匹配. 期望: \"" + expectedOutputTrimmed + "\", 实际: \"" + actualOutput + "\"");
            }

        } catch (Exception e) {
            log.error("测试用例执行失败: testCase={}", testCaseId, e);
            result.setStatus(JudgeStatus.SYSTEM_ERROR);
            result.setScore(0);
            result.setErrorMessage("系统错误: " + e.getMessage());
        }

        return result;
    }

    private String[] getExecuteCommand(LanguageType language) {
        return switch (language) {
            case JAVA -> new String[] { "java", String.format("-Xmx%dm", 128), "-cp", "/tmp/classes", "Main" }; // 使用新的编译输出目录
            default -> new String[] {}; // Should not happen if all languages are handled
        };
    }
}