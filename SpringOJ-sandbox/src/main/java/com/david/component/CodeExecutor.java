package com.david.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.david.constants.SandboxConstants;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码执行器 - 负责在Docker容器中编译和执行代码
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeExecutor {
    
    private final DockerClient dockerClient;
    
    /**
     * 编译代码
     * 
     * @param containerId 容器ID
     * @param language 编程语言
     * @param request 沙箱执行请求
     * @return 编译是否成功
     */
    public boolean compileCode(String containerId, LanguageType language, SandboxExecuteRequest request) {
        log.debug("开始编译代码: containerId={}, language={}", containerId, language);
        
        try {
            var compileCommand = getCompileCommand(language);
            if (compileCommand.length == 0) {
                log.debug("语言 {} 无需编译", language);
                return true;
            }
            
            var execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(compileCommand)
                    .withAttachStdout(true)
                    .withAttachStderr(true);
            
            var execCreateResponse = execCreateCmd.exec();
            var output = captureExecutionOutput(execCreateResponse.getId());
            
            if (!output.stderr().isEmpty()) {
                log.error("编译失败: {}", output.stderr());
                return false;
            }
            
            log.info(SandboxConstants.LogMessages.COMPILATION_SUCCESS, containerId);
            return true;
            
        } catch (Exception e) {
            log.error("编译过程异常: containerId={}", containerId, e);
            return false;
        }
    }
    
    /**
     * 执行程序
     * 
     * @param containerId 容器ID
     * @param input 输入数据
     * @param language 编程语言
     * @param request 沙箱执行请求
     * @return 执行结果
     */
    public ExecutionResult executeProgram(String containerId, String input, LanguageType language, 
                                        SandboxExecuteRequest request) {
        log.debug("开始执行程序: containerId={}, language={}", containerId, language);
        
        try {
            var executeCommand = getExecuteCommand(language, request.getMemoryLimit());
            
            var execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd(executeCommand)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true);
            
            var execCreateResponse = execCreateCmd.exec();
            
            long startTime = System.currentTimeMillis();
            var maxMemoryUsage = new AtomicLong(0);
            
            // 启动内存监控
            var memoryMonitor = startMemoryMonitoring(containerId, maxMemoryUsage);
            
            try {
                var output = executeWithTimeout(execCreateResponse.getId(), input, request.getTimeLimit());
                long timeUsed = System.currentTimeMillis() - startTime;
                
                log.info(SandboxConstants.LogMessages.EXECUTION_COMPLETED, containerId, timeUsed);
                
                return ExecutionResult.builder()
                        .stdout(output.stdout())
                        .stderr(output.stderr())
                        .timeUsed(timeUsed)
                        .memoryUsed(maxMemoryUsage.get())
                        .status(output.stderr().isEmpty() ? JudgeStatus.ACCEPTED : JudgeStatus.RUNTIME_ERROR)
                        .completed(true)
                        .build();
                        
            } finally {
                stopMemoryMonitoring(memoryMonitor);
            }
            
        } catch (Exception e) {
            log.error("程序执行异常: containerId={}", containerId, e);
            return ExecutionResult.builder()
                    .stderr(e.getMessage())
                    .status(JudgeStatus.SYSTEM_ERROR)
                    .completed(false)
                    .build();
        }
    }
    
    /**
     * 带超时的执行
     */
    private OutputCapture executeWithTimeout(String execId, String input, int timeLimit) throws Exception {
        var outputCapture = new OutputCapture();
        
        var callback = new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDOUT) {
                    outputCapture.appendStdout(new String(frame.getPayload()));
                } else if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDERR) {
                    outputCapture.appendStderr(new String(frame.getPayload()));
                }
            }
        };
        
        var execStartCmd = dockerClient.execStartCmd(execId)
                .withDetach(false)
                .withStdIn(input != null ? new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)) : null);
        
        execStartCmd.exec(callback);
        
        // 等待执行完成或超时
        boolean completed = callback.awaitCompletion(timeLimit, TimeUnit.MILLISECONDS);
        
        if (!completed) {
            callback.close();
            throw new RuntimeException(SandboxConstants.ErrorMessages.EXECUTION_TIMEOUT);
        }
        
        return new OutputCapture(outputCapture.getStdout(), outputCapture.getStderr());
    }
    
    /**
     * 启动内存监控
     */
    private Thread startMemoryMonitoring(String containerId, AtomicLong maxMemoryUsage) {
        var monitorThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    var stats = dockerClient.statsCmd(containerId).exec(null);
                    // 这里应该解析内存使用情况，简化处理
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("内存监控异常: containerId={}", containerId, e);
            }
        });
        
        monitorThread.start();
        return monitorThread;
    }
    
    /**
     * 停止内存监控
     */
    private void stopMemoryMonitoring(Thread monitorThread) {
        if (monitorThread != null && monitorThread.isAlive()) {
            monitorThread.interrupt();
        }
    }
    
    /**
     * 捕获命令执行输出
     */
    private OutputCapture captureExecutionOutput(String execId) throws Exception {
        var outputCapture = new OutputCapture();
        
        var callback = new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDOUT) {
                    outputCapture.appendStdout(new String(frame.getPayload()));
                } else if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDERR) {
                    outputCapture.appendStderr(new String(frame.getPayload()));
                }
            }
        };
        
        dockerClient.execStartCmd(execId).withDetach(false).exec(callback);
        callback.awaitCompletion();
        
        return outputCapture;
    }
    
    /**
     * 获取编译命令
     */
    private String[] getCompileCommand(LanguageType language) {
        return switch (language) {
            case JAVA -> new String[]{"sh", "-c", SandboxConstants.JavaConfig.COMPILE_COMMAND};
            default -> new String[]{};
        };
    }
    
    /**
     * 获取执行命令
     */
    private String[] getExecuteCommand(LanguageType language, int memoryLimit) {
        return switch (language) {
            case JAVA -> new String[]{
                SandboxConstants.JavaConfig.EXECUTE_COMMAND_PREFIX,
                String.format(SandboxConstants.JavaConfig.JVM_MEMORY_PARAM, memoryLimit),
                SandboxConstants.JavaConfig.CLASSPATH_PARAM,
                SandboxConstants.DockerConfig.COMPILE_OUTPUT_DIR,
                SandboxConstants.JavaConfig.MAIN_CLASS
            };
            default -> new String[]{};
        };
    }
    
    /**
     * 执行结果记录
     */
    public record ExecutionResult(
            String stdout,
            String stderr,
            long timeUsed,
            long memoryUsed,
            JudgeStatus status,
            boolean completed
    ) {
        public static ExecutionResultBuilder builder() {
            return new ExecutionResultBuilder();
        }
        
        public static class ExecutionResultBuilder {
            private String stdout = "";
            private String stderr = "";
            private long timeUsed = 0;
            private long memoryUsed = 0;
            private JudgeStatus status = JudgeStatus.ACCEPTED;
            private boolean completed = true;
            
            public ExecutionResultBuilder stdout(String stdout) {
                this.stdout = stdout;
                return this;
            }
            
            public ExecutionResultBuilder stderr(String stderr) {
                this.stderr = stderr;
                return this;
            }
            
            public ExecutionResultBuilder timeUsed(long timeUsed) {
                this.timeUsed = timeUsed;
                return this;
            }
            
            public ExecutionResultBuilder memoryUsed(long memoryUsed) {
                this.memoryUsed = memoryUsed;
                return this;
            }
            
            public ExecutionResultBuilder status(JudgeStatus status) {
                this.status = status;
                return this;
            }
            
            public ExecutionResultBuilder completed(boolean completed) {
                this.completed = completed;
                return this;
            }
            
            public ExecutionResult build() {
                return new ExecutionResult(stdout, stderr, timeUsed, memoryUsed, status, completed);
            }
        }
    }
    
    /**
     * 输出捕获器
     */
    private static class OutputCapture {
        private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        
        public OutputCapture() {}
        
        public OutputCapture(String stdout, String stderr) {
            this.stdout.writeBytes(stdout.getBytes());
            this.stderr.writeBytes(stderr.getBytes());
        }
        
        public void appendStdout(String output) {
            try {
                stdout.write(output.getBytes());
            } catch (IOException e) {
                log.warn("写入stdout失败", e);
            }
        }
        
        public void appendStderr(String output) {
            try {
                stderr.write(output.getBytes());
            } catch (IOException e) {
                log.warn("写入stderr失败", e);
            }
        }
        
        public String getStdout() {
            return stdout.toString();
        }
        
        public String getStderr() {
            return stderr.toString();
        }
        
        public String stdout() {
            return getStdout();
        }
        
        public String stderr() {
            return getStderr();
        }
    }
}
