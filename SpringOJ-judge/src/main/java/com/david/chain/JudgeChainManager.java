package com.david.chain;

import org.springframework.stereotype.Component;

import com.david.chain.handler.ExecutionHandler;
import com.david.chain.handler.PreparationHandler;
import com.david.chain.handler.RequestBuildHandler;
import com.david.chain.handler.StatusUpdateHandler;
import com.david.chain.handler.ValidationHandler;
import com.david.dto.JudgeContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 判题责任链管理器 - 负责构建和管理判题处理链
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeChainManager {

    private final ValidationHandler validationHandler;
    private final StatusUpdateHandler statusUpdateHandler;
    private final PreparationHandler preparationHandler;
    private final RequestBuildHandler requestBuildHandler;
    private final ExecutionHandler executionHandler;

    /**
     * 构建判题处理链
     * 
     * @return 责任链的第一个处理器
     */
    public JudgeChainHandler buildJudgeChain() {
        log.debug("构建判题责任链");

        // 构建责任链：验证 -> 状态更新 -> 准备数据 -> 构建请求 -> 执行
        validationHandler
                .setNext(statusUpdateHandler)
                .setNext(preparationHandler)
                .setNext(requestBuildHandler)
                .setNext(executionHandler);

        log.debug("判题责任链构建完成");
        return validationHandler;
    }

    /**
     * 执行完整的判题流程
     * 
     * @param context 判题上下文
     */
    public void executeJudgeChain(JudgeContext context) {
        log.info("开始执行判题责任链: submissionId={}",
                context.getSubmission() != null ? context.getSubmission().getId() : "unknown");

        try {
            JudgeChainHandler chain = buildJudgeChain();
            chain.handle(context);

            if (context.shouldContinueProcessing()) {
                log.info("判题责任链执行成功: submissionId={}", context.getSubmission().getId());
            } else {
                log.warn("判题责任链执行中断: submissionId={}, error={}",
                        context.getSubmission().getId(), context.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("判题责任链执行失败: submissionId={}",
                    context.getSubmission() != null ? context.getSubmission().getId() : "unknown", e);
            throw e;
        }
    }
}
