package com.david.chain.handler.Java;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.JudgmentResult;
import com.david.enums.JudgeStatus;
import com.david.enums.interfaces.LimitType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JavaCompareHandler extends Handler {

    /**
     * 解析毫秒数字，支持形如 "123ms" 或 "123" 的字符串，解析失败返回 null。
     */
    private static Long parseNumberMs(String s) {
        return parseLeadingNumber(s);
    }

    /**
     * 解析 MB 数字，支持形如 "64MB"、"64" 的字符串，解析失败返回 null。
     */
    private static Long parseNumberMb(String s) {
        return parseLeadingNumber(s);
    }

    /**
     * 提取字符串前缀的连续数字（忽略后续单位），无数字或异常则返回 null。
     * 使用 util ReUtil 提高健壮性。
     */
    private static Long parseLeadingNumber(String s) {
        if (StrUtil.isBlank(s))
            return null;
        String digits = ReUtil.get("^(\\d+)", s.trim(), 1);
        if (StrUtil.isBlank(digits))
            return null;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 终态对比处理器：
     * 1) 基于 JavaRunHandler 已生成的 JudgmentResult 列表进行时间和内存限制检查；
     * 2) 更新每个 JudgmentResult 的最终状态（时间超限/内存超限/通过）；
     * 3) 设置 JudgmentContext 的最终状态。
     * <p>
     * 注：JavaRunHandler 已完成输出正确性检查，此处主要进行资源限制检查。
     *
     * @param judgmentContext 评测上下文，包含 JavaRunHandler 生成的 JudgmentResult 列表
     * @return 当所有测试用例都通过且未超时/超内存时返回 true；否则返回 false。
     */
    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        try {
            if (judgmentContext == null) {
                log.error("JavaCompareHandler: JudgmentContext is null");
                return false;
            }

            List<JudgmentResult> judgmentResults = judgmentContext.getJudgmentResults();
            if (judgmentResults == null || judgmentResults.isEmpty()) {
                log.warn("JavaCompareHandler: No JudgmentResults found, skipping resource limit checks");
                return createErrorAndExit(judgmentContext, "缺少测试用例执行结果");
            }

            LimitType limitType = judgmentContext.getLimitType();
            if (limitType == null) {
                log.warn("JavaCompareHandler: LimitType is null, skipping resource limit checks");
                // 无限制时直接基于现有结果判断
                return finalizeResults(judgmentContext, judgmentResults);
            }

            // 对每个 JudgmentResult 进行资源限制检查
            boolean allPassed = true;
            int passedCount = 0;
            int failedCount = 0;
            JudgeStatus finalStatus = JudgeStatus.ACCEPTED;

            for (JudgmentResult result : judgmentResults) {
                // 跳过已经是错误状态的结果（如编译错误、运行错误等）
                if (result.getJudgeStatus() != null &&
                        result.getJudgeStatus() != JudgeStatus.WRONG_ANSWER &&
                        result.getJudgeStatus() != JudgeStatus.ACCEPTED) {
                    allPassed = false;
                    failedCount++;
                    continue;
                }

                // 检查时间限制
                Long usedMs = parseNumberMs(result.getTimeUsed());
                long limitMs = limitType.getTimeLimitMillis();
                if (usedMs != null && usedMs > limitMs) {
                    result.setJudgeStatus(JudgeStatus.TIME_LIMIT_EXCEEDED);
                    result.setJudgeInfo((result.getJudgeInfo() != null ? result.getJudgeInfo() + "\n" : "") +
                            "时间超限: " + usedMs + "ms > " + limitMs + "ms");
                    allPassed = false;
                    failedCount++;
                    finalStatus = JudgeStatus.TIME_LIMIT_EXCEEDED;
                    continue;
                }

                // 检查内存限制
                Long usedMb = parseNumberMb(result.getMemoryUsed());
                long limitMb = limitType.getMemoryLimitMB();
                if (usedMb != null && usedMb > limitMb) {
                    result.setJudgeStatus(JudgeStatus.MEMORY_LIMIT_EXCEEDED);
                    result.setJudgeInfo((result.getJudgeInfo() != null ? result.getJudgeInfo() + "\n" : "") +
                            "内存超限: " + usedMb + "MB > " + limitMb + "MB");
                    allPassed = false;
                    failedCount++;
                    finalStatus = JudgeStatus.MEMORY_LIMIT_EXCEEDED;
                    continue;
                }

                // 如果之前是 WRONG_ANSWER，保持该状态
                if (result.getJudgeStatus() == JudgeStatus.WRONG_ANSWER) {
                    allPassed = false;
                    failedCount++;
                    finalStatus = JudgeStatus.WRONG_ANSWER;
                } else {
                    // 通过所有检查，设置为 ACCEPTED
                    result.setJudgeStatus(JudgeStatus.ACCEPTED);
                    passedCount++;
                }
            }

            // 设置最终状态和信息
            judgmentContext.setJudgeStatus(allPassed ? JudgeStatus.ACCEPTED : finalStatus);

            String statusInfo = String.format("资源限制检查完成 - 通过: %d, 失败: %d, 总计: %d",
                    passedCount, failedCount, judgmentResults.size());

            String originalInfo = judgmentContext.getJudgeInfo();
            judgmentContext.setJudgeInfo(originalInfo != null ? originalInfo + "\n" + statusInfo : statusInfo);

            log.info("JavaCompareHandler completed: {}", statusInfo);

            // 继续责任链或返回终态
            if (this.nextHandler != null) {
                return this.nextHandler.handleRequest(judgmentContext);
            }
            return allPassed;

        } catch (Exception e) {
            log.error("JavaCompareHandler处理异常", e);
            return createErrorAndExit(judgmentContext, "资源限制检查阶段出错: " + e.getMessage());
        }
    }

    /**
     * 当无 LimitType 时，基于现有结果进行最终判定
     */
    private boolean finalizeResults(JudgmentContext context, List<JudgmentResult> results) {
        boolean allPassed = true;
        JudgeStatus finalStatus = JudgeStatus.ACCEPTED;

        for (JudgmentResult result : results) {
            if (result.getJudgeStatus() == null) {
                result.setJudgeStatus(JudgeStatus.ACCEPTED);
            } else if (result.getJudgeStatus() != JudgeStatus.ACCEPTED) {
                allPassed = false;
                finalStatus = result.getJudgeStatus();
            }
        }

        context.setJudgeStatus(finalStatus);
        return allPassed;
    }

    /**
     * 创建错误状态并退出
     */
    private boolean createErrorAndExit(JudgmentContext context, String errorMsg) {
        context.setJudgeInfo(errorMsg);
        context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
        log.error("JavaCompareHandler error: {}", errorMsg);
        return false;
    }
}
