package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.enums.JudgeStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Java 代码编译处理器
 * 
 * 功能：
 * 1. 将格式化后的代码写入临时文件
 * 2. 调用 javac 进行编译
 * 3. 捕获编译错误信息
 * 4. 管理编译超时
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaCompileHandler extends Handler {
    
    private static final int COMPILE_TIMEOUT_SECONDS = 30;
    private static final String TEMP_DIR_PREFIX = "springoj-compile-";
    private static final String MAIN_CLASS_NAME = "Main.java";
    
    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        Path tempDir = null;
        try {
            log.info("开始编译 Java 代码，提交ID: {}", judgmentContext.getSubmissionId());
            
            // 验证必要参数
            if (!validateContext(judgmentContext)) {
                return false;
            }
            
            // 创建临时目录
            tempDir = createTempDirectory();
            
            // 写入代码文件
            Path sourceFile = writeSourceCode(tempDir, judgmentContext.getRunCode());
            
            // 执行编译
            CompileResult result = compileJavaCode(sourceFile, tempDir);
            
            // 处理编译结果
            if (!handleCompileResult(result, judgmentContext)){
				return false;
            }
			return nextHandler.handleRequest(judgmentContext);
            
        } catch (Exception e) {
            log.error("编译过程发生异常，提交ID: {}, 错误: {}", 
                     judgmentContext.getSubmissionId(), e.getMessage(), e);
            
            judgmentContext.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            judgmentContext.setCompileInfo("编译过程发生系统错误: " + e.getMessage());
            return false;
            
        } finally {
            // 清理临时目录
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }
    
    /**
     * 验证判题上下文
     */
    private boolean validateContext(JudgmentContext context) {
        if (context.getRunCode() == null || context.getRunCode().trim().isEmpty()) {
            log.error("待编译代码为空，提交ID: {}", context.getSubmissionId());
            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setCompileInfo("待编译代码不能为空");
            return false;
        }
        return true;
    }
    
    /**
     * 创建临时编译目录
     */
    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory(TEMP_DIR_PREFIX);
    }
    
    /**
     * 将源代码写入文件
     */
    private Path writeSourceCode(Path tempDir, String sourceCode) throws IOException {
        Path sourceFile = tempDir.resolve(MAIN_CLASS_NAME);
        Files.write(sourceFile, sourceCode.getBytes("UTF-8"));
        return sourceFile;
    }
    
    /**
     * 编译 Java 代码
     */
    private CompileResult compileJavaCode(Path sourceFile, Path tempDir) {
        try {
            // 构建编译命令
            List<String> command = new ArrayList<>();
            command.add("javac");
            command.add("-encoding");
            command.add("UTF-8");
            command.add("-cp");
            command.add(buildClasspath());
            command.add("-d");
            command.add(tempDir.toString());
            command.add(sourceFile.toString());
            
            log.debug("编译命令: {}", String.join(" ", command));
            
            // 创建进程
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(tempDir.toFile());
            processBuilder.redirectErrorStream(true); // 将错误流重定向到输出流
            
            // 启动编译进程
            Process process = processBuilder.start();
            
            // 使用线程池处理超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<CompileResult> future = executor.submit(() -> {
                try {
                    // 读取编译输出
                    String output = readProcessOutput(process);
                    
                    // 等待进程结束
                    int exitCode = process.waitFor();
                    
                    return new CompileResult(exitCode == 0, exitCode, output);
                    
                } catch (Exception e) {
                    throw new RuntimeException("编译过程异常", e);
                }
            });
            
            try {
                // 等待编译完成，设置超时
                CompileResult result = future.get(COMPILE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                executor.shutdown();
                return result;
                
            } catch (TimeoutException e) {
                log.warn("编译超时，强制终止进程");
                process.destroyForcibly();
                future.cancel(true);
                executor.shutdownNow();
                
                return new CompileResult(false, -1, "编译超时（超过 " + COMPILE_TIMEOUT_SECONDS + " 秒）");
            }
            
        } catch (Exception e) {
            log.error("编译过程发生异常", e);
            return new CompileResult(false, -1, "编译过程发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 读取进程输出
     */
    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
    
    /**
     * 构建类路径
     */
    private String buildClasspath() {
        // 包含 Jackson 库路径，用于 JSON 处理
        List<String> classpaths = new ArrayList<>();
        
        // 系统类路径
        String systemClasspath = System.getProperty("java.class.path");
        if (systemClasspath != null && !systemClasspath.isEmpty()) {
            classpaths.add(systemClasspath);
        }
        
        // 当前目录
        classpaths.add(".");
        
        return String.join(File.pathSeparator, classpaths);
    }
    
    /**
     * 处理编译结果
     */
    private boolean handleCompileResult(CompileResult result, JudgmentContext context) {
        if (result.isSuccess()) {
            log.info("编译成功，提交ID: {}", context.getSubmissionId());
            context.setJudgeStatus(JudgeStatus.CONTINUE);
            context.setCompileInfo("编译成功");
            return true;
            
        } else {
            log.info("编译失败，提交ID: {}, 错误信息: {}", 
                    context.getSubmissionId(), result.getOutput());
            
            context.setJudgeStatus(JudgeStatus.COMPILE_ERROR);
            context.setCompileInfo(formatCompileError(result.getOutput()));
            return false;
        }
    }
    
    /**
     * 格式化编译错误信息
     */
    private String formatCompileError(String rawError) {
        if (rawError == null || rawError.trim().isEmpty()) {
            return "编译失败，但未获取到具体错误信息";
        }
        
        // 移除临时文件路径信息，只保留相对错误信息
        String cleaned = rawError.replaceAll("/tmp/[^/]+/", "")
                                 .replaceAll("\\\\[^\\\\]+\\\\", "");
        
        // 限制错误信息长度，避免过长
        if (cleaned.length() > 2000) {
            cleaned = cleaned.substring(0, 2000) + "...\n[错误信息过长，已截断]";
        }
        
        return cleaned.trim();
    }
    
    /**
     * 清理临时目录
     */
    private void cleanupTempDirectory(Path tempDir) {
        try {
            // 递归删除临时目录及其内容
            Files.walk(tempDir)
                 .sorted((a, b) -> b.getNameCount() - a.getNameCount()) // 先删除文件，后删除目录
                 .forEach(path -> {
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
    
    /**
     * 编译结果封装类
     */
    private static class CompileResult {
        private final boolean success;
        private final int exitCode;
        private final String output;
        
        public CompileResult(boolean success, int exitCode, String output) {
            this.success = success;
            this.exitCode = exitCode;
            this.output = output;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public int getExitCode() {
            return exitCode;
        }
        
        public String getOutput() {
            return output;
        }
    }
}
