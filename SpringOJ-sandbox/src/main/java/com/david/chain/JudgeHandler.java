package com.david.chain;

import com.david.chain.utils.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.JudgeStatus;
import com.david.strategy.records.ExecutionResult;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 判题处理器抽象基类
 */
public abstract class JudgeHandler {

    protected JudgeHandler nextHandler;

    public JudgeHandler setNext(JudgeHandler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public final JudgeResult handle(JudgeContext context) {
        JudgeResult result = doHandle(context);

        // 如果当前处理失败，直接返回
        if (result.getStatus() != JudgeStatus.ACCEPTED &&
                result.getStatus() != JudgeStatus.CONTINUE) {
            return result;
        }

        // 如果有下一个处理器，继续处理
        if (nextHandler != null) {
            return nextHandler.handle(context);
        }

        return result;
    }

    protected abstract JudgeResult doHandle(JudgeContext context);

    // =======================
    // 通用结果构建工具方法
    // =======================
    protected JudgeResult continueResult() {
        return JudgeResult.builder()
                .status(JudgeStatus.CONTINUE)
                .build();
    }

    protected JudgeResult errorResult(SandboxExecuteRequest request, String message) {
        return JudgeResult.builder()
                .submissionId(request != null ? request.getSubmissionId() : null)
                .status(JudgeStatus.SYSTEM_ERROR)
                .score(0)
                .timeUsed(0)
                .memoryUsed(0)
                .errorMessage(message)
                .build();
    }

    // =======================
    // Docker 执行与输出捕获工具
    // =======================
    protected ExecutionResult captureExecutionOutput(JudgeContext context, String execId, long timeoutMs)
            throws InterruptedException {
        final StringBuilder stdout = new StringBuilder();
        final StringBuilder stderr = new StringBuilder();

        final Charset charset = resolveOutputCharset(context);

        long start = System.currentTimeMillis();

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {
                if (frame != null) {
                    if (frame.getStreamType() == StreamType.STDERR) {
                        stderr.append(new String(frame.getPayload(), charset));
                    } else {
                        stdout.append(new String(frame.getPayload(), charset));
                    }
                }
            }
        };

        context.getDockerClient()
                .execStartCmd(execId)
                .withDetach(false)
                .withTty(false)
                .exec(callback);

        boolean completed = callback.awaitCompletion(timeoutMs, TimeUnit.MILLISECONDS);
        long timeUsed = System.currentTimeMillis() - start;

        return new ExecutionResult(stdout.toString(), stderr.toString(), timeUsed, completed);
    }

    private Charset resolveOutputCharset(JudgeContext context) {
        try {
            var strategy = context != null ? context.getLanguageStrategy() : null;
            var cfg = strategy != null ? strategy.getOutputConfig() : null;
            String enc = (cfg != null && cfg.getOutputEncoding() != null && !cfg.getOutputEncoding().isBlank())
                    ? cfg.getOutputEncoding()
                    : "UTF-8";
            return Charset.forName(enc);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    protected ExecutionResult executeWithInput(JudgeContext context, String execId, String input)
            throws InterruptedException {
        final StringBuilder stdout = new StringBuilder();
        final StringBuilder stderr = new StringBuilder();

        long start = System.currentTimeMillis();

        final Charset charset = resolveOutputCharset(context);

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {
                if (frame != null) {
                    if (frame.getStreamType() == StreamType.STDERR) {
                        stderr.append(new String(frame.getPayload(), charset));
                    } else {
                        stdout.append(new String(frame.getPayload(), charset));
                    }
                }
            }
        };

        var startCmd = context.getDockerClient()
                .execStartCmd(execId)
                .withDetach(false)
                .withTty(false);

        if (input != null) {
            startCmd.withStdIn(new java.io.ByteArrayInputStream((input + "\n").getBytes(charset))); 
        }

        startCmd.exec(callback);

        long timeoutMs = context.getRequest() != null && context.getRequest().getTimeLimit() != null
                ? context.getRequest().getTimeLimit()
                : 60_000; // 默认超时 60s
        boolean completed = callback.awaitCompletion(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        long timeUsed = System.currentTimeMillis() - start;

        return new ExecutionResult(stdout.toString(), stderr.toString(), timeUsed, completed);
    }

    // =======================
    // Docker 辅助命令
    // =======================
    protected void setDirectoryPermissions(JudgeContext context, String dir) {
        try {
            var resp = context.getDockerClient()
                    .execCreateCmd(context.getContainerId())
                    .withCmd("sh", "-c", "chmod -R 755 " + dir + " && chown -R 0:0 " + dir)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            captureExecutionOutput(context, resp.getId(), 30_000);
        } catch (Exception ignored) {
            // 权限调整失败不应中断整体流程，记录日志由调用方处理
        }
    }

    protected void executeInitCommand(JudgeContext context, String initCmd) {
        if (initCmd == null || initCmd.isBlank()) return;
        executeCommand(context, new String[]{"sh", "-c", initCmd});
    }

    protected void executeCommand(JudgeContext context, String[] cmd) {
        try {
            var resp = context.getDockerClient()
                    .execCreateCmd(context.getContainerId())
                    .withCmd(cmd)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            captureExecutionOutput(context, resp.getId(), 60_000);
        } catch (Exception ignored) {
            // 非致命命令失败不应中断流程
        }
    }

    // =======================
    // 内存监控（基于 Docker stats，适配所有语言）
    // =======================
    protected com.david.strategy.utils.MemoryMonitor startMemoryMonitoring(JudgeContext context) {
        try {
            var docker = context != null ? context.getDockerClient() : null;
            var containerId = context != null ? context.getContainerId() : null;

            var maxMem = new java.util.concurrent.atomic.AtomicLong(0L);
            var monitor = com.david.strategy.utils.MemoryMonitor.builder()
                    .maxMemoryUsage(maxMem)
                    .monitoring(true)
                    .build();

            if (docker == null || containerId == null || containerId.isBlank()) {
                return monitor; // 无法监控但不影响判题流程
            }

            ResultCallback.Adapter<Statistics> callback = new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Statistics stats) {
                    // 若已停止，关闭回调
                    if (!monitor.isMonitoring()) {
                        try { close(); } catch (Exception ignored) {}
                        return;
                    }
                    try {
                        long usage = 0L;
                        var mem = stats.getMemoryStats();
                        if (mem != null) {
                            try {
                                Long u = mem.getUsage();
                                if (u == null) u = mem.getMaxUsage();
                                if (u != null) usage = u;
                            } catch (Exception ignored) { }
                        }
                        long prev;
                        do {
                            prev = maxMem.get();
                            if (usage <= prev) break;
                        } while (!maxMem.compareAndSet(prev, usage));
                    } catch (Exception ignored) { }
                }
            };

            monitor.setStatsCallback(callback);
            docker.statsCmd(containerId)
                    .withNoStream(false) // 持续流式统计
                    .exec(callback);

            return monitor;
        } catch (Exception e) {
            return com.david.strategy.utils.MemoryMonitor.builder()
                    .maxMemoryUsage(new java.util.concurrent.atomic.AtomicLong(0L))
                    .monitoring(true)
                    .build();
        }
    }

    protected long stopMemoryMonitoring(com.david.strategy.utils.MemoryMonitor monitor) {
        if (monitor == null) return 0L;
        monitor.stopMonitoring();
        long max = monitor.getMaxMemoryUsage() != null ? monitor.getMaxMemoryUsage().get() : 0L;
        return Math.max(0L, max);
    }
}
