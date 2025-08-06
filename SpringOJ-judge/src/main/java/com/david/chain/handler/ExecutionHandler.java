package com.david.chain.handler;

import org.springframework.stereotype.Component;

import com.david.chain.AbstractJudgeChainHandler;
import com.david.constants.JudgeConstants;
import com.david.dto.JudgeContext;
import com.david.producer.SandboxProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行处理器 - 负责将沙箱请求发送到执行队列
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionHandler extends AbstractJudgeChainHandler {
    
    private final SandboxProducer sandboxProducer;
    
    @Override
    protected void doHandle(JudgeContext context) {
        var submission = context.getSubmission();
        var sandboxRequest = context.getSandboxRequest();
        
        log.info("开始发送沙箱执行请求: submissionId={}", submission.getId());
        
        try {
            // 发送到沙箱执行队列
            sandboxProducer.executeInSandbox(sandboxRequest);
            
            log.info(JudgeConstants.LogMessages.JUDGE_REQUEST_SENT,
                    submission.getId(), 
                    context.getProblem().getId(), 
                    submission.getLanguage());
            
        } catch (Exception e) {
            log.error("发送沙箱执行请求失败: submissionId={}", submission.getId(), e);
            throw e;
        }
    }
}
