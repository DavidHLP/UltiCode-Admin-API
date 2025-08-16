package com.david.consumer;

import com.david.chain.utils.JudgmentContext;
import com.david.enums.CodeLimitType;
import com.david.enums.JudgeStatus;
import com.david.producer.JudgeProducer;
import com.david.strategy.CodeRunFactory;
import com.david.submission.Submission;
import com.david.submission.dto.SubmitToSandboxRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxConsumer {
    private final CodeRunFactory codeRunFactory;
    private final JudgeProducer judgeProducer;

    /**
     * 监听评测请求并触发对应语言的策略执行。
     *
     * @param request 从上游提交服务投递的评测请求
     */
    @KafkaListener(topics = "submit_to_sandbox_request", groupId = "sandbox-consumer-group")
    public void executionJudgment(SubmitToSandboxRequest request) {
        if (request == null) {
            log.warn("收到空的 SubmitToSandboxRequest，忽略");
            return;
        }
        try {
            JudgmentContext ctx =
                    JudgmentContext.builder()
                            .problemId(request.getProblemId())
                            .submissionId(request.getSubmissionId())
                            .solutionFunctionName(request.getSolutionFunctionName())
                            .userId(request.getUserId())
                            .language(request.getLanguage())
                            .limitType(
                                    CodeLimitType.fromLanguage(request.getLanguage())
                                            .getLimit(request.getDifficulty()))
                            .judgeStatus(JudgeStatus.JUDGING)
                            .solutionCode(request.getSourceCode())
                            .testCases(request.getTestCases())
                            .build();

            Boolean passed = codeRunFactory.execute(request.getLanguage(), ctx);
            log.info(
                    "评测完成：submissionId={}, problemId={}, passed={}, judgeInfo={}",
                    ctx.getSubmissionId(),
                    ctx.getProblemId(),
                    passed,
                    ctx.getJudgeInfo());

            // 构造 Submission 并发送更新
            JudgeStatus finalStatus = ctx.getJudgeStatus();
            if (finalStatus == null) {
                // 兜底：根据 passed 推断
                finalStatus =
                        Boolean.TRUE.equals(passed)
                                ? JudgeStatus.ACCEPTED
                                : JudgeStatus.WRONG_ANSWER;
            }

            Submission submission =
                    Submission.builder()
                            .id(ctx.getSubmissionId())
                            .userId(ctx.getUserId())
                            .problemId(ctx.getProblemId())
                            .language(ctx.getLanguage())
                            .sourceCode(ctx.getSolutionCode())
                            .status(finalStatus)
                            .timeUsed(parseMsToInt(ctx.getTimeUsed()))
                            .memoryUsed(parseMemToKb(ctx.getMemoryUsed()))
                            .compileInfo(ctx.getCompileInfo())
                            .judgeInfo(ctx.getJudgeInfo())
                            .build();
            if (ctx.getJudgeStatus() == JudgeStatus.WRONG_ANSWER) {
                submission.setErrorTestCaseId(ctx.getErrorTestCaseId());
                submission.setErrorTestCaseOutput(ctx.getErrorTestCaseOutput());
                submission.setErrorTestCaseExpectOutput(ctx.getErrorTestCaseExpectOutput());
            }

            judgeProducer.updateSubmission(submission);
        } catch (Exception e) {
            log.error(
                    "评测执行异常：submissionId={}, problemId={}, language={}, msg={}",
                    request.getSubmissionId(),
                    request.getProblemId(),
                    request.getLanguage(),
                    e.getMessage(),
                    e);
            judgeProducer.updateSubmission(
                    Submission.builder()
                            .id(request.getSubmissionId())
                            .status(JudgeStatus.SYSTEM_ERROR)
                            .judgeInfo(e.getMessage())
                            .build());
        }
    }

    // 解析如 "123ms" 或 "123" -> 123（ms），失败返回 null
    private Integer parseMsToInt(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        int i = 0;
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        if (i == 0) return null;
        try {
            long v = Long.parseLong(s.substring(0, i));
            if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return (int) v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 解析内存字符串到 KB：支持 "64MB"、"64000KB"、"64"（按 MB 解释兼容运行阶段格式），失败返回 null
    private Integer parseMemToKb(String s) {
        if (s == null) return null;
        s = s.trim().toUpperCase();
        if (s.isEmpty()) return null;
        int i = 0;
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        if (i == 0) return null;
        long num;
        try {
            num = Long.parseLong(s.substring(0, i));
        } catch (NumberFormatException e) {
            return null;
        }
        String unit = s.substring(i).trim();
        long kb;
        if (unit.startsWith("KB")) {
            kb = num;
        } else {
            // 默认按 MB 处理（运行阶段记录通常为 "xxMB"）
            kb = num * 1024L;
        }
        if (kb > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) kb;
    }
}
