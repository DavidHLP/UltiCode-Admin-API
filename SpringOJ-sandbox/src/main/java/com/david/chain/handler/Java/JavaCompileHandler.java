package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.JudgmentResult;
import com.david.config.JudgeProperties;
import com.david.enums.JudgeStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

@Component
@RequiredArgsConstructor
public class JavaCompileHandler extends Handler {

    private final JudgeProperties judgeProperties;

    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        try {
            if (judgmentContext == null) {
                return false;
            }

            // 1) 校验 runCode
            if (!validateRunCode(judgmentContext)) {
                return false;
            }

            // 2) 准备工作目录与源码文件
            Path workDir = prepareWorkDir(judgmentContext.getSubmissionId());
            Path srcFile = writeSourceFile(workDir, judgmentContext.getRunCode());

            // 3) 编译（带超时），记录失败/超时耗时
            int timeoutSec = Math.max(1, judgeProperties.getDefaultCompileTimeout());
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            long startNs = System.nanoTime();
            boolean success;
            try {
                success = doCompile(srcFile, workDir, diagnostics, timeoutSec);
            } catch (TimeoutException te) {
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                judgmentContext.setTimeUsed(String.valueOf(elapsedMs));
                judgmentContext.setCompileInfo("编译超时(>" + timeoutSec + "s)");
                return createErrorAndExit(judgmentContext, "编译超时");
            } catch (Exception e) {
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
                judgmentContext.setTimeUsed(String.valueOf(elapsedMs));
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                if (e instanceof IllegalStateException) {
                    judgmentContext.setCompileInfo(msg);
                    return createErrorAndExit(judgmentContext, "编译环境错误: " + msg);
                } else {
                    judgmentContext.setCompileInfo("编译异常: " + msg);
                    return createErrorAndExit(judgmentContext, "编译异常: " + msg);
                }
            }

            // 4) 计算编译耗时
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            judgmentContext.setTimeUsed(String.valueOf(elapsedMs));

            // 5) 汇总诊断并判断结果
            String diagMsg = buildDiagnosticsMessage(diagnostics);
            if (!success) {
                String compileMsg = diagMsg.isEmpty() ? "编译失败" : diagMsg;
                judgmentContext.setCompileInfo(compileMsg);
                return createErrorAndExit(judgmentContext, "编译失败\n" + compileMsg);
            }

            // 6) 生成详细的编译成功信息
            String compileSuccessInfo = buildCompileSuccessInfo(workDir, srcFile, elapsedMs, diagnostics);
            judgmentContext.setCompileInfo(compileSuccessInfo);

            // 7) 继续责任链
            if (this.nextHandler != null) {
                return this.nextHandler.handleRequest(judgmentContext);
            }
            return true;
        } catch (Exception e) {
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            return createErrorAndExit(judgmentContext, "编译阶段出错: " + msg + "\n详细信息: " + e.getClass().getSimpleName());
        }
    }

    /**
     * 创建错误状态并退出
     */
    private boolean createErrorAndExit(JudgmentContext context, String errorMsg) {
        context.setJudgeInfo(errorMsg);
        context.setJudgeStatus(JudgeStatus.COMPILE_ERROR);

        // 为所有测试用例创建统一的错误 JudgmentResult
        List<com.david.testcase.TestCase> testCases = context.getTestCases();
        List<JudgmentResult> results = new ArrayList<>();

        if (testCases != null && !testCases.isEmpty()) {
            for (com.david.testcase.TestCase testCase : testCases) {
                results.add(createErrorJudgmentResult(testCase.getId(), errorMsg, context));
            }
        } else {
            // 无测试用例时创建一个通用错误结果
            results.add(createErrorJudgmentResult(null, errorMsg, context));
        }

        context.setJudgmentResults(results);
        return false;
    }

    /**
     * 创建错误状态的 JudgmentResult
     */
    private JudgmentResult createErrorJudgmentResult(Long testCaseId, String errorMsg, JudgmentContext context) {
        return JudgmentResult.builder()
                .testCaseId(testCaseId)
                .judgeStatus(JudgeStatus.COMPILE_ERROR)
                .memoryUsed(context.getMemoryUsed())
                .timeUsed(context.getTimeUsed())
                .compileInfo(context.getCompileInfo())
                .judgeInfo(errorMsg)
                .build();
    }

    // 校验 runCode 是否存在
    private boolean validateRunCode(JudgmentContext ctx) {
        String runCode = ctx.getRunCode();
        if (runCode == null || runCode.trim().isEmpty()) {
            ctx.setJudgeInfo("缺少 runCode：请先执行 JavaFormatCodeHandler");
            return false;
        }
        return true;
    }

    // 准备工作目录: {workDir}/java/{submissionId}
    private Path prepareWorkDir(Long submissionId) throws IOException {
        String sidStr = (submissionId == null
                ? String.valueOf(System.currentTimeMillis())
                : String.valueOf(submissionId));
        Path workDir = Paths.get(judgeProperties.getWorkDir(), "java", sidStr);
        Files.createDirectories(workDir);
        return workDir;
    }

    // 写入源码文件 Main.java
    private Path writeSourceFile(Path workDir, String runCode) throws IOException {
        Path srcFile = workDir.resolve("Main.java");
        Files.writeString(srcFile, runCode, StandardCharsets.UTF_8);
        return srcFile;
    }

    // 构建 javac 参数
    private List<String> buildJavacOptions(Path workDir) {
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-d");
        options.add(workDir.toString());
        options.add("-classpath");
        options.add(System.getProperty("java.class.path", "."));
        return options;
    }

    // 执行编译（带超时），返回是否成功；可能抛出 TimeoutException 或其他异常
    private boolean doCompile(
            Path srcFile,
            Path workDir,
            DiagnosticCollector<JavaFileObject> diagnostics,
            int timeoutSec)
            throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK 编译器不可用：请在 JDK 环境下运行或提供 javac");
        }

        List<String> options = buildJavacOptions(workDir);
        Callable<Boolean> compileCallable = () -> {
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                    diagnostics, Locale.getDefault(), StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(srcFile.toFile());
                JavaCompiler.CompilationTask task = compiler.getTask(
                        null,
                        fileManager,
                        diagnostics,
                        options,
                        null,
                        compilationUnits);
                return Boolean.TRUE.equals(task.call());
            }
        };

        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = es.submit(compileCallable);
            return future.get(timeoutSec, TimeUnit.SECONDS);
        } finally {
            es.shutdownNow();
        }
    }

    // 构建诊断信息字符串
    private String buildDiagnosticsMessage(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder diagMsg = new StringBuilder();
        for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
            String srcName;
            Object srcObj = d.getSource();
            if (srcObj instanceof JavaFileObject) {
                srcName = ((JavaFileObject) srcObj).getName();
            } else if (srcObj != null) {
                srcName = srcObj.toString();
            } else {
                srcName = "<unknown>";
            }

            diagMsg.append("[")
                    .append(d.getKind())
                    .append("] ")
                    .append(srcName)
                    .append(": line ")
                    .append(d.getLineNumber())
                    .append(", col ")
                    .append(d.getColumnNumber())
                    .append(" - ")
                    .append(d.getMessage(Locale.getDefault()))
                    .append('\n');
        }
        return diagMsg.toString();
    }

    /**
     * 构建编译成功的详细信息
     */
    private String buildCompileSuccessInfo(Path workDir, Path srcFile, long elapsedMs,
            DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder info = new StringBuilder();

        try {
            // 1. 基础编译信息
            info.append("编译成功");

            // 2. 编译耗时
            info.append(" (").append(elapsedMs).append("ms)");

            // 3. 源文件信息
            if (Files.exists(srcFile)) {
                long srcSize = Files.size(srcFile);
                info.append(", 源文件: ").append(formatFileSize(srcSize));
            }

            // 4. 生成的字节码文件信息
            Path classFile = workDir.resolve("Main.class");
            if (Files.exists(classFile)) {
                long classSize = Files.size(classFile);
                info.append(", 字节码: ").append(formatFileSize(classSize));
            }

            // 5. 警告信息统计
            int warningCount = 0;
            int noteCount = 0;
            for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                switch (d.getKind()) {
                    case WARNING -> warningCount++;
                    case NOTE, MANDATORY_WARNING -> noteCount++;
                    case ERROR -> {
                    } // 错误在编译失败时已处理
                    case OTHER -> {
                    } // 其他类型诊断信息，通常不需要特殊处理
                }
            }

            if (warningCount > 0) {
                info.append(", 警告: ").append(warningCount).append("个");
            }
            if (noteCount > 0) {
                info.append(", 提示: ").append(noteCount).append("个");
            }

            // 6. 如果有警告或提示，追加详细信息
            if (warningCount > 0 || noteCount > 0) {
                info.append("\n详细信息:\n");
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.WARNING ||
                            d.getKind() == Diagnostic.Kind.NOTE ||
                            d.getKind() == Diagnostic.Kind.MANDATORY_WARNING) {
                        info.append("  [").append(d.getKind()).append("] line ")
                                .append(d.getLineNumber()).append(": ")
                                .append(d.getMessage(Locale.getDefault())).append("\n");
                    }
                }
            }

        } catch (IOException e) {
            // 如果获取文件信息失败，回退到基础信息
            return "编译成功 (" + elapsedMs + "ms)";
        }

        return info.toString();
    }

    /**
     * 格式化文件大小显示
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        }
    }
}
