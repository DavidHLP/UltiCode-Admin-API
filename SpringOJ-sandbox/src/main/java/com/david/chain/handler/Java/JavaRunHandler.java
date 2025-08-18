package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.JudgmentResult;
import com.david.chain.utils.RunResult;
import com.david.enums.JudgeStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Java 代码运行处理器
 *
 * <p>功能： 1. 执行编译后的 Java 程序 2. 解析 JSON 格式的运行结果 3. 处理运行时错误和超时 4. 监控资源使用情况
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaRunHandler extends Handler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int RUN_TIMEOUT_SECONDS = 10;
    private static final String TEMP_DIR_PREFIX = "springoj-run-";
    private static final String MAIN_CLASS_NAME = "Main";
    // 限制输出，防止输出泛洪导致宿主 OOM（每个流 1MB）
    private static final int MAX_STREAM_BYTES = 1_000_000;
    // ulimit 限制：文件描述符/线程数（作为 fork/线程爆炸防护）
    private static final int ULIMIT_NOFILE = 64;
    private static final int ULIMIT_NPROC = 64;
    // ulimit 虚拟内存上限（KB），与 -Xmx 共同限制——此值略大于堆大小，给运行时与线程栈预留
    private static final int ULIMIT_VMEM_KB = 512 * 1024; // 512MB

    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        Path tempDir = null;
        try {
            log.info("开始运行 Java 代码，提交ID: {}", judgmentContext.getSubmissionId());

            // 验证必要参数
            if (!validateContext(judgmentContext)) {
                return false;
            }

            // 创建临时运行目录
            tempDir = createTempDirectory();

            // 复制编译后的代码到运行目录
            copyCompiledCode(tempDir, judgmentContext.getRunCode());

            // 执行程序
            RunResult result = executeJavaProgram(tempDir);

            // 处理运行结果
            if (!handleRunResult(result, judgmentContext)) {
                return false;
            }
            return nextHandler.handleRequest(judgmentContext);

        } catch (Exception e) {
            log.error(
                    "运行过程发生异常，提交ID: {}, 错误: {}",
                    judgmentContext.getSubmissionId(),
                    e.getMessage(),
                    e);

            judgmentContext.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            judgmentContext.setJudgeInfo("运行过程发生系统错误: " + e.getMessage());
            return false;

        } finally {
            // 清理临时目录
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    /** 验证判题上下文 */
    private boolean validateContext(JudgmentContext context) {
        if (context.getJudgeStatus() != JudgeStatus.CONTINUE) {
            log.error(
                    "前置处理未成功，当前状态: {}, 提交ID: {}",
                    context.getJudgeStatus(),
                    context.getSubmissionId());
            return false;
        }

        if (context.getRunCode() == null || context.getRunCode().trim().isEmpty()) {
            log.error("待运行代码为空，提交ID: {}", context.getSubmissionId());
            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setJudgeInfo("待运行代码不能为空");
            return false;
        }

        return true;
    }

    /** 创建临时运行目录 */
    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory(TEMP_DIR_PREFIX);
    }

    /** 复制编译后的代码到运行目录 */
    private void copyCompiledCode(Path tempDir, String sourceCode) throws IOException {
        // 写入源代码文件
        Path sourceFile = tempDir.resolve("Main.java");
        Files.writeString(sourceFile, sourceCode);

        // 编译代码到运行目录
        compileCodeInRunDirectory(tempDir, sourceFile);
    }

    /** 在运行目录中编译代码 */
    private void compileCodeInRunDirectory(Path tempDir, Path sourceFile) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-encoding");
        command.add("UTF-8");
        command.add("-cp");
        command.add(buildSafeClasspath(tempDir));
        command.add(sourceFile.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(tempDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("运行时编译失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("编译过程被中断", e);
        }
    }

    /** 执行 Java 程序 */
    private RunResult executeJavaProgram(Path tempDir) {
        try {
            // 仅暴露用户代码与 Jackson 依赖到 classpath，避免访问服务端依赖
            String classpath = buildSafeClasspath(tempDir);

            // 通过 bash + ulimit 进行资源约束
            String ulimitCmd = String.format(
                    "ulimit -t %d -v %d -n %d -u %d; exec java -XX:+UseSerialGC -Xmx256m -Xss1m -cp '%s' %s",
                    RUN_TIMEOUT_SECONDS + 1, ULIMIT_VMEM_KB, ULIMIT_NOFILE, ULIMIT_NPROC, classpath, MAIN_CLASS_NAME);

            List<String> command = new ArrayList<>();
            command.add("bash");
            command.add("-lc");
            command.add(ulimitCmd);

            log.debug("运行命令(bash -lc): {}", ulimitCmd);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(tempDir.toFile());
            processBuilder.redirectErrorStream(false);
            // 收紧环境变量，避免泄露宿主信息
            processBuilder.environment().clear();
            processBuilder.environment().put("LANG", "C.UTF-8");
            processBuilder.environment().put("PATH", "/usr/bin:/bin");

            // 启动运行进程
            Process process = processBuilder.start();

            // 使用线程池处理超时和IO
            ExecutorService executor = Executors.newFixedThreadPool(3);

            // 监控子进程内存（峰值 RSS）
            AtomicLong peakRssKb = new AtomicLong(0);
            Thread memWatcher = new Thread(() -> monitorProcessMemory(process, peakRssKb));
            memWatcher.setDaemon(true);
            memWatcher.start();

            // 异步读取输出流
            Future<String> outputFuture =
                    executor.submit(() -> readStream(process.getInputStream(), MAX_STREAM_BYTES));

            // 异步读取错误流
            Future<String> errorFuture =
                    executor.submit(() -> readStream(process.getErrorStream(), MAX_STREAM_BYTES));

            // 异步等待进程结束
            Future<Integer> processFuture =
                    executor.submit(
                            () -> {
                                try {
                                    return process.waitFor();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new RuntimeException("进程等待被中断", e);
                                }
                            });

            try {
                // 等待进程完成，设置超时
                long startTime = System.currentTimeMillis();
                Integer exitCode = processFuture.get(RUN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                long endTime = System.currentTimeMillis();

                // 获取输出和错误信息
                String output = outputFuture.get(1, TimeUnit.SECONDS);
                String error = errorFuture.get(1, TimeUnit.SECONDS);

                executor.shutdown();

                return new RunResult(
                        exitCode == 0,
                        exitCode,
                        output,
                        error,
                        endTime - startTime,
                        Math.max(0, peakRssKb.get() / 1024)); // 转换为 MB

            } catch (TimeoutException e) {
                log.warn("程序运行超时，强制终止进程");
                destroyProcessTree(process);

                // 取消所有异步任务
                outputFuture.cancel(true);
                errorFuture.cancel(true);
                processFuture.cancel(true);
                executor.shutdownNow();

                return new RunResult(
                        false,
                        -1,
                        "",
                        "程序运行超时（超过 " + RUN_TIMEOUT_SECONDS + " 秒）",
                        RUN_TIMEOUT_SECONDS * 1000L,
                        Math.max(0, peakRssKb.get() / 1024));
            }

        } catch (Exception e) {
            log.error("程序运行过程发生异常", e);
            return new RunResult(false, -1, "", "程序运行过程发生异常: " + e.getMessage(), 0L, 0L);
        }
    }

    /** 读取流内容 */
    private String readStream(InputStream inputStream, int maxBytes) {
        byte[] buffer = new byte[8192];
        int read;
        int total = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = inputStream) {
            while ((read = in.read(buffer)) != -1) {
                int allowed = Math.min(read, Math.max(0, maxBytes - total));
                if (allowed > 0) {
                    baos.write(buffer, 0, allowed);
                    total += allowed;
                }
                if (total >= maxBytes) {
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("读取流内容时发生错误", e);
        }
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        if (total >= maxBytes) {
            return content + "\n...[TRUNCATED OUTPUT]";
        }
        return content.trim();
    }

    /** 构建类路径 */
    private String buildSafeClasspath(Path tempDir) {
        List<String> cps = new ArrayList<>();
        // 用户编译产物所在目录
        cps.add(tempDir.toString());

        // 仅引入 Jackson 依赖，满足生成代码的 JSON 需求
        String sysCp = System.getProperty("java.class.path");
        if (sysCp != null && !sysCp.isEmpty()) {
            String[] entries = sysCp.split(Pattern.quote(File.pathSeparator));
            for (String e : entries) {
                String name = new File(e).getName();
                if (name.startsWith("jackson-") || name.startsWith("jackson-databind")
                        || name.startsWith("jackson-core") || name.startsWith("jackson-annotations")) {
                    cps.add(e);
                }
            }
        }
        return String.join(File.pathSeparator, cps);
    }

    /** 监控子进程内存峰值（读取 /proc/<pid>/status 的 VmRSS） */
    private void monitorProcessMemory(Process process, AtomicLong peakRssKb) {
        long pid = process.pid();
        Path status = Path.of("/proc", String.valueOf(pid), "status");
        while (process.isAlive()) {
            try {
                List<String> lines = Files.readAllLines(status);
                for (String l : lines) {
                    if (l.startsWith("VmRSS:")) {
                        String[] parts = l.trim().split("\\s+");
                        if (parts.length >= 2) {
                            long kb = Long.parseLong(parts[1]);
                            peakRssKb.updateAndGet(prev -> Math.max(prev, kb));
                        }
                        break;
                    }
                }
                Thread.sleep(50);
            } catch (Exception ignored) {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }
    }

    /** 强制销毁进程树，避免子进程遗留 */
    private void destroyProcessTree(Process process) {
        try {
            ProcessHandle handle = process.toHandle();
            handle.descendants().forEach(ph -> {
                try { ph.destroyForcibly(); } catch (Exception ignored) {}
            });
            handle.destroyForcibly();
        } catch (Exception e) {
            log.warn("销毁进程树失败，回退到直接销毁", e);
            process.destroyForcibly();
        }
    }

    /** 处理运行结果 */
    private boolean handleRunResult(RunResult result, JudgmentContext context) {
        if (!result.isSuccess()) {
            // 运行失败
            log.info("程序运行失败，提交ID: {}, 错误信息: {}", context.getSubmissionId(), result.getError());

            context.setJudgeStatus(JudgeStatus.RUNTIME_ERROR);
            context.setJudgeInfo("运行时错误: " + result.getError());
            context.setTimeUsed(String.valueOf(result.getExecutionTime()));
            context.setMemoryUsed(String.valueOf(result.getMemoryUsage()));

            return false;
        }

        // 运行成功，解析输出结果
        try {
            List<JudgmentResult> judgmentResults = parseRunOutput(result.getOutput(), context);

            context.setJudgmentResults(judgmentResults);
            context.setTimeUsed(String.valueOf(result.getExecutionTime()));
            context.setMemoryUsed(String.valueOf(result.getMemoryUsage()));
            context.setJudgeStatus(JudgeStatus.CONTINUE);

            log.info(
                    "程序运行成功，提交ID: {}, 测试用例数: {}",
                    context.getSubmissionId(),
                    judgmentResults.size());

            return true;

        } catch (Exception e) {
            log.error("解析运行结果失败，提交ID: {}, 错误: {}", context.getSubmissionId(), e.getMessage(), e);

            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setJudgeInfo("解析运行结果失败: " + e.getMessage());
            return false;
        }
    }

    /** 解析程序运行输出 */
    private List<JudgmentResult> parseRunOutput(String output, JudgmentContext context)
            throws JsonProcessingException {

        if (output == null || output.trim().isEmpty()) {
            throw new IllegalArgumentException("程序输出为空");
        }

        String trimmedOutput = output.trim();

        // 期望输出格式：[{"testCaseIndex":0,"success":true,"output":"..."}]
        JsonNode rootNode = MAPPER.readTree(trimmedOutput);

        if (!rootNode.isArray()) {
            throw new IllegalArgumentException("程序输出不是 JSON 数组格式");
        }

        List<JudgmentResult> results = new ArrayList<>();

        for (JsonNode resultNode : rootNode) {
            JudgmentResult judgmentResult = parseTestCaseResult(resultNode, context);
            results.add(judgmentResult);
        }

        return results;
    }

    /** 解析单个测试用例结果 */
    private JudgmentResult parseTestCaseResult(JsonNode resultNode, JudgmentContext context) {
        JudgmentResult result = new JudgmentResult();

        // 解析测试用例索引
        if (resultNode.has("testCaseIndex")) {
            int testCaseIndex = resultNode.get("testCaseIndex").asInt();
            if (testCaseIndex >= 0 && testCaseIndex < context.getTestCases().size()) {
                result.setTestCaseId(context.getTestCases().get(testCaseIndex).getId());
            }
        }

        // 解析成功状态
        boolean success = resultNode.has("success") && resultNode.get("success").asBoolean();

        if (success) {
            result.setJudgeStatus(JudgeStatus.ACCEPTED);
            result.setJudgeInfo("答案正确");

            if (resultNode.has("output")) {
                // 实际输出（用于后续比较验证）
                String actualOutput = resultNode.get("output").asText();
                result.setCompileInfo("实际输出: " + actualOutput);
            }

        } else {
            result.setJudgeStatus(JudgeStatus.WRONG_ANSWER);

            if (resultNode.has("error")) {
                result.setJudgeInfo("运行时错误: " + resultNode.get("error").asText());
            } else if (resultNode.has("actualOutput") && resultNode.has("expectedOutput")) {
                String actualOutput = resultNode.get("actualOutput").asText();
                String expectedOutput = resultNode.get("expectedOutput").asText();
                result.setJudgeInfo(
                        String.format("答案错误\n期望输出: %s\n实际输出: %s", expectedOutput, actualOutput));
                // 设置 compileInfo 以供 JavaCompareHandler 提取实际输出
                result.setCompileInfo("实际输出: " + actualOutput);
            } else {
                result.setJudgeInfo("答案错误");
            }
        }

        // 设置资源使用信息
        result.setTimeUsed(context.getTimeUsed());
        result.setMemoryUsed(context.getMemoryUsed());

        return result;
    }

    /** 清理临时目录 */
    private void cleanupTempDirectory(Path tempDir) {
        try (Stream<Path> walkStream = Files.walk(tempDir)) {
            walkStream
                    .sorted((Path a, Path b) -> b.getNameCount() - a.getNameCount())
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    log.warn("清理临时文件失败: {}", path, e);
                                }
                            });
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", tempDir, e);
        }
    }
}
