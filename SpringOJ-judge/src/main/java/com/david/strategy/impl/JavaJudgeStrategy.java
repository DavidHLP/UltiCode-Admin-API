package com.david.strategy.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.david.domain.dto.JudgeContext;
import com.david.domain.dto.JudgeResult;
import com.david.domain.entity.TestCase;
import com.david.domain.enums.SubmissionStatus;
import com.david.strategy.JudgeStrategy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JavaJudgeStrategy implements JudgeStrategy {

    private static final String WORK_DIR_PREFIX = "/tmp/judge/";
    private static final String JAVA_CLASS_NAME = "Main";

    @Override
    public JudgeResult execute(JudgeContext context) {
        String code = context.getSubmission().getSourceCode();
        Integer timeLimit = context.getProblem().getTimeLimit();
        List<TestCase> testCases = context.getTestCases();

        String runId = UUID.randomUUID().toString(true);
        String workDirPath = WORK_DIR_PREFIX + runId;
        File workDir = new File(workDirPath);
        if (!workDir.mkdirs()) {
            return JudgeResult.builder().status(SubmissionStatus.SYSTEM_ERROR).errorOutput("创建判题工作目录失败").build();
        }

        File sourceFile = new File(workDirPath, JAVA_CLASS_NAME + ".java");
        FileUtil.writeString(code, sourceFile, StandardCharsets.UTF_8);

        try {
            Process compileProcess = new ProcessBuilder("javac", sourceFile.getAbsolutePath())
                    .directory(workDir)
                    .redirectErrorStream(true)
                    .start();

            boolean compiled = compileProcess.waitFor(5, TimeUnit.SECONDS);
            if (!compiled || compileProcess.exitValue() != 0) {
                String compileError = new String(compileProcess.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                FileUtil.del(workDir);
                return JudgeResult.builder().status(SubmissionStatus.COMPILE_ERROR).errorOutput(compileError).build();
            }
        } catch (IOException | InterruptedException e) {
            FileUtil.del(workDir);
            return JudgeResult.builder().status(SubmissionStatus.SYSTEM_ERROR).errorOutput(e.getMessage()).build();
        }

        List<JudgeResult> results = new ArrayList<>();
        for (TestCase testCase : testCases) {
            results.add(runTestCase(workDir, testCase, timeLimit));
        }

        FileUtil.del(workDir);

        return aggregateResults(results);
    }

    private JudgeResult runTestCase(File workDir, TestCase testCase, Integer timeLimit) {
        try {
            long startTime = System.currentTimeMillis();
            Process runProcess = new ProcessBuilder("java", "-cp", ".", JAVA_CLASS_NAME)
                    .directory(workDir)
                    .start();

            if (testCase.getInputFile() != null) {
                runProcess.getOutputStream().write(testCase.getInputFile().getBytes(StandardCharsets.UTF_8));
                runProcess.getOutputStream().close();
            }

            boolean finished = runProcess.waitFor(timeLimit, TimeUnit.MILLISECONDS);
            long timeUsed = System.currentTimeMillis() - startTime;

            if (!finished) {
                runProcess.destroyForcibly();
                return JudgeResult.builder().status(SubmissionStatus.TIME_LIMIT_EXCEEDED).time(timeUsed).build();
            }

            String output = new String(runProcess.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String errorOutput = new String(runProcess.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

            if (runProcess.exitValue() != 0) {
                return JudgeResult.builder().status(SubmissionStatus.RUNTIME_ERROR).errorOutput(errorOutput).time(timeUsed).build();
            }

            if (!testCase.getOutputFile().trim().equals(output.trim())) {
                return JudgeResult.builder().status(SubmissionStatus.WRONG_ANSWER).output(output).time(timeUsed).build();
            }

            return JudgeResult.builder().status(SubmissionStatus.ACCEPTED).time(timeUsed).build();

        } catch (IOException | InterruptedException e) {
            return JudgeResult.builder().status(SubmissionStatus.SYSTEM_ERROR).errorOutput(e.getMessage()).build();
        }
    }

    private JudgeResult aggregateResults(List<JudgeResult> results) {
        long maxTime = 0;
        long maxMemory = 0; // 内存占用暂未实现

        for (JudgeResult result : results) {
            if (result.getStatus() != SubmissionStatus.ACCEPTED) {
                return result;
            }
            if (result.getTime() > maxTime) {
                maxTime = result.getTime();
            }
        }

        return JudgeResult.builder()
                .status(SubmissionStatus.ACCEPTED)
                .time(maxTime)
                .memory(maxMemory)
                .build();
    }
}
