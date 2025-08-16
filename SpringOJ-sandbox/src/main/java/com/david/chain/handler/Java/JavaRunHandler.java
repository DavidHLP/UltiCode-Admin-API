package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.JudgmentResult;
import com.david.config.JudgeProperties;
import com.david.enums.JudgeStatus;
import com.david.enums.interfaces.LimitType;
import com.david.testcase.TestCase;
import com.david.testcase.TestCaseOutput;
import com.david.utils.java.JavaFormationUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JavaRunHandler extends Handler {

    private final JudgeProperties judgeProperties;

    // 读取 Linux /proc/<pid>/status 的内存占用（优先 VmHWM，其次 VmRSS），单位：kB
    private static Long readProcMemKb(long pid) {
        try {
            Path status = Paths.get("/proc", String.valueOf(pid), "status");
            if (!Files.exists(status))
                return null;
            java.util.List<String> lines = Files.readAllLines(status);
            Long rss = null;
            for (String raw : lines) {
                String line = raw.trim();
                if (line.startsWith("VmHWM:")) {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (!part.isEmpty() && part.chars().allMatch(Character::isDigit)) {
                            try {
                                return Long.parseLong(part);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                } else if (line.startsWith("VmRSS:")) {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (!part.isEmpty() && part.chars().allMatch(Character::isDigit)) {
                            try {
                                rss = Long.parseLong(part);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
            return rss; // 若无 HWM，则返回 RSS（当前值）
        } catch (Exception ignored) {
            return null;
        }
    }

    // 将 kB 转换为 MB 数值（最小 1MB）
    private static long kbToMBValue(long kb) {
        return Math.max(1L, kb / 1024L);
    }

    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        try {
            if (judgmentContext == null) {
                return false;
            }

            // 计算编译产物目录，与 JavaCompileHandler 保持一致
            Long sid = judgmentContext.getSubmissionId();
            String sidStr = (sid == null ? String.valueOf(System.currentTimeMillis()) : String.valueOf(sid));
            Path workDir = Paths.get(judgeProperties.getWorkDir(), "java", sidStr);
            Files.createDirectories(workDir);

            // 组合 java 命令
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome == null ? "java" : Paths.get(javaHome, "bin", "java").toString();

            String classpath = workDir + File.pathSeparator + System.getProperty("java.class.path", ".");

            // 依据 LimitType 设置运行资源限制
            LimitType lt = judgmentContext.getLimitType();
            int xmx = Math.max(16, lt.getMemoryLimitMB()); // 至少 16MB
            List<String> cmd = new ArrayList<>();
            cmd.add(javaBin);
            cmd.add("-Xmx" + xmx + "m");
            cmd.add("-cp");
            cmd.add(classpath);
            cmd.add("Main");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(workDir.toFile());

            // 记录启动时间用于统计整体运行耗时
            long startNs = System.nanoTime();
            Process p = pb.start();

            // 读取输出（单行：实际结果JSON），并控制超时
            StringBuilder fullOut = new StringBuilder();
            String resultLine = null;

            // 运行超时：使用 LimitType 的毫秒限制；保证最小 100ms
            int timeoutMs;
            long limitMsLong = lt.getTimeLimitMillis();
            timeoutMs = (limitMsLong > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) limitMsLong;
            timeoutMs = Math.max(100, timeoutMs);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                long start = System.nanoTime();
                String line;
                while (true) {
                    if (br.ready() && (line = br.readLine()) != null) {
                        if (resultLine == null && !line.trim().isEmpty()) {
                            resultLine = line;
                        }
                        fullOut.append(line).append('\n');
                    } else {
                        if (p.waitFor(10, TimeUnit.MILLISECONDS)) {
                            // 进程结束，尝试读尽缓冲
                            while (br.ready() && (line = br.readLine()) != null) {
                                if (resultLine == null && !line.trim().isEmpty()) {
                                    resultLine = line;
                                }
                                fullOut.append(line).append('\n');
                            }
                            break;
                        }
                        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                        if (elapsedMs > timeoutMs) {
                            p.destroyForcibly();
                            // 记录整体耗时（从进程启动起）
                            long totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                            judgmentContext.setTimeUsed(String.valueOf(totalMs));
                            // 记录内存使用（尽力获取，获取失败则回退为 -Xmx）
                            Long memKbTimeout = readProcMemKb(p.pid());
                            if (memKbTimeout != null) {
                                judgmentContext.setMemoryUsed(String.valueOf(kbToMBValue(memKbTimeout)));
                            } else {
                                judgmentContext.setMemoryUsed(String.valueOf(xmx));
                            }
                            return createErrorAndExit(judgmentContext, "运行超时(>" + timeoutMs + "ms)",
                                    JudgeStatus.TIME_LIMIT_EXCEEDED);
                        }
                    }
                }
            }

            // 记录整体耗时与内存占用（若可获取）
            long elapsedTotalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            judgmentContext.setTimeUsed(String.valueOf(elapsedTotalMs));
            Long memKb = readProcMemKb(p.pid());
            if (memKb != null) {
                judgmentContext.setMemoryUsed(String.valueOf(kbToMBValue(memKb)));
            } else {
                judgmentContext.setMemoryUsed(String.valueOf(xmx));
            }

            int exit = p.exitValue();
            if (exit != 0) {
                return createErrorAndExit(judgmentContext, "运行异常，exitCode=" + exit + "\n" + fullOut,
                        JudgeStatus.RUNTIME_ERROR);
            }

            if (resultLine == null) {
                return createErrorAndExit(judgmentContext, "运行输出缺失：未获取到结果\n" + fullOut, JudgeStatus.RUNTIME_ERROR);
            }

            // 解析多测试用例的 JSON 数组结果并与期望值比对
            List<TestCase> testCases = judgmentContext.getTestCases();
            if (testCases == null || testCases.isEmpty()) {
                return createErrorAndExit(judgmentContext, "运行完成，但缺少测试用例用于比对，输出=" + resultLine,
                        JudgeStatus.SYSTEM_ERROR);
            }

            ObjectMapper mapper = new ObjectMapper();
            List<JudgmentResult> judgmentResults = new ArrayList<>();

            try {
                // 解析运行结果为 JSON 数组
                JsonNode resultArray = mapper.readTree(resultLine);
                if (!resultArray.isArray()) {
                    return createErrorAndExit(judgmentContext, "运行输出格式错误：期望 JSON 数组，实际=" + resultLine,
                            JudgeStatus.RUNTIME_ERROR);
                }

                // fail-fast 模式下，如果某个测试用例失败，后续测试用例不会执行，结果数量可能少于测试用例数量
                if (resultArray.size() > testCases.size()) {
                    return createErrorAndExit(judgmentContext,
                            "运行结果数量超过测试用例数量：期望<=" + testCases.size() + "，实际=" + resultArray.size(),
                            JudgeStatus.RUNTIME_ERROR);
                }

                // 逐个比对已执行的测试用例结果
                boolean hasFailedCase = false;
                for (int i = 0; i < resultArray.size(); i++) {
                    TestCase testCase = testCases.get(i);
                    TestCaseOutput expectedOutput = testCase.getTestCaseOutput();

                    if (expectedOutput == null) {
                        judgmentResults.add(createErrorJudgmentResult(
                                testCase.getId(), "测试用例[" + testCase.getId() + "]缺少期望输出",
                                judgmentContext, JudgeStatus.SYSTEM_ERROR));
                        continue;
                    }

                    String expectedRaw = Objects.toString(expectedOutput.getOutput(), "");
                    String outputType = expectedOutput.getOutputType();
                    String expectedJson = JavaFormationUtils.ensureJsonLiteral(expectedRaw, outputType);

                    JsonNode actualResult = resultArray.get(i);
                    boolean isCorrect;
                    String errorMsg = null;
                    String actualOutput;

                    try {
                        // 检查是否为错误结果
                        if (actualResult.has("error")) {
                            isCorrect = false;
                            String errorText = actualResult.get("error").asText();
                            errorMsg = "运行时错误: " + errorText;
                            actualOutput = mapper.writeValueAsString(errorText);
                        } else if (actualResult.has("success") && actualResult.get("success").asBoolean()) {
                            // 成功的测试用例：提取实际输出进行比对
                            JsonNode outputNode = actualResult.get("output");
                            if (outputNode != null) {
                                // 使用 Jackson 标准序列化确保格式正确
                                actualOutput = mapper.writeValueAsString(outputNode);
                            } else {
                                // 如果没有 output 字段，序列化整个结果节点
                                actualOutput = mapper.writeValueAsString(actualResult);
                            }
                            JsonNode expectedNode = mapper.readTree(expectedJson);
                            JsonNode actualNode = mapper.readTree(actualOutput);
                            isCorrect = Objects.equals(expectedNode, actualNode);
                        } else {
                            // 失败的测试用例：直接标记为错误
                            isCorrect = false;
                            JsonNode actualOutputNode = actualResult.get("actualOutput");
                            if (actualOutputNode != null) {
                                actualOutput = mapper.writeValueAsString(actualOutputNode);
                            } else {
                                // 序列化整个结果节点
                                actualOutput = mapper.writeValueAsString(actualResult);
                            }
                            errorMsg = "测试用例失败";
                        }
                    } catch (Exception parseEx) {
                        isCorrect = false;
                        try {
                            actualOutput = mapper.writeValueAsString(actualResult);
                        } catch (Exception serEx) {
                            actualOutput = "序列化失败";
                        }
                        errorMsg = "结果解析失败: " + parseEx.getMessage();
                    }

                    JudgeStatus status = isCorrect ? null : JudgeStatus.WRONG_ANSWER; // null表示待后续时间/内存检查决定
                    String info = errorMsg != null ? errorMsg
                            : ("输出比对:" + (isCorrect ? "通过" : "不通过") + ", 期望=" + expectedJson + ", 实际=" + actualOutput);

                    judgmentResults.add(JudgmentResult.builder()
                            .testCaseId(testCase.getId())
                            .judgeStatus(status)
                            .memoryUsed(judgmentContext.getMemoryUsed())
                            .timeUsed(judgmentContext.getTimeUsed())
                            .compileInfo(judgmentContext.getCompileInfo())
                            .judgeInfo(info)
                            .build());

                    // 只记录第一个失败的测试用例信息到 JudgmentContext（用于 fail-fast 模式）
                    if (!isCorrect) {
                        judgmentContext.setErrorTestCaseId(testCase.getId());
                        judgmentContext.setErrorTestCaseOutput(actualOutput != null ? actualOutput : "未知");
                        judgmentContext.setErrorTestCaseExpectOutput(expectedJson);
                        // fail-fast: 遇到第一个错误就停止处理后续测试用例
                        break;
                    }
                }

                // 对于 fail-fast 模式下未执行的测试用例，创建 "未执行" 状态的结果
                for (int i = resultArray.size(); i < testCases.size(); i++) {
                    TestCase testCase = testCases.get(i);
                    judgmentResults.add(JudgmentResult.builder()
                            .testCaseId(testCase.getId())
                            .judgeStatus(JudgeStatus.SYSTEM_ERROR) // 使用 SYSTEM_ERROR 表示未执行
                            .memoryUsed(judgmentContext.getMemoryUsed())
                            .timeUsed(judgmentContext.getTimeUsed())
                            .compileInfo(judgmentContext.getCompileInfo())
                            .judgeInfo("fail-fast模式下未执行：因前序测试用例失败而跳过")
                            .build());
                }

                judgmentContext.setJudgmentResults(judgmentResults);
                judgmentContext.setJudgeInfo("运行完成，处理了 " + testCases.size() + " 个测试用例");

            } catch (Exception parseEx) {
                return createErrorAndExit(judgmentContext, "解析运行结果失败: " + parseEx.getMessage() + ", 输出=" + resultLine,
                        JudgeStatus.RUNTIME_ERROR);
            }

            // 继续责任链
            if (this.nextHandler != null) {
                return this.nextHandler.handleRequest(judgmentContext);
            }
            return true;
        } catch (IOException | InterruptedException e) {
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            return createErrorAndExit(judgmentContext, "运行阶段出错: " + msg + "\n详细信息: " + e.getClass().getSimpleName(),
                    JudgeStatus.RUNTIME_ERROR);
        }
    }

    /**
     * 创建错误状态的 JudgmentResult
     */
    private JudgmentResult createErrorJudgmentResult(Long testCaseId, String errorMsg,
            JudgmentContext context, JudgeStatus status) {
        return JudgmentResult.builder()
                .testCaseId(testCaseId)
                .judgeStatus(status)
                .memoryUsed(context.getMemoryUsed())
                .timeUsed(context.getTimeUsed())
                .compileInfo(context.getCompileInfo())
                .judgeInfo(errorMsg)
                .build();
    }

    /**
     * 创建错误状态并退出
     */
    private boolean createErrorAndExit(JudgmentContext context, String errorMsg, JudgeStatus status) {
        context.setJudgeInfo(errorMsg);
        context.setJudgeStatus(status);

        // 为所有测试用例创建统一的错误 JudgmentResult
        List<TestCase> testCases = context.getTestCases();
        List<JudgmentResult> results = new ArrayList<>();

        if (testCases != null && !testCases.isEmpty()) {
            for (TestCase testCase : testCases) {
                results.add(createErrorJudgmentResult(testCase.getId(), errorMsg, context, status));
            }
        } else {
            // 无测试用例时创建一个通用错误结果
            results.add(createErrorJudgmentResult(null, errorMsg, context, status));
        }

        context.setJudgmentResults(results);
        return false;
    }
}
