package com.david.chain.handler;

import org.springframework.stereotype.Component;

import com.david.chain.AbstractJudgeChainHandler;
import com.david.dto.JudgeContext;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.judge.enums.JudgeStatus;
import com.david.utils.ResponseValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 状态更新处理器 - 负责更新提交记录的判题状态
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusUpdateHandler extends AbstractJudgeChainHandler {
    
    private final SubmissionServiceFeignClient submissionServiceFeignClient;
    
    @Override
    protected void doHandle(JudgeContext context) {
        var submission = context.getSubmission();
        
        log.info("开始更新提交状态: submissionId={}", submission.getId());
        
        try {
            // 更新状态为判题中
            submission.setStatus(JudgeStatus.JUDGING);
            
            var response = submissionServiceFeignClient.updateSubmission(submission.getId(), submission);
            ResponseValidator.validateOrThrow(
                response, 
                "更新提交状态失败", 
                submission.getId()
            );
            
            log.info("提交状态更新成功: submissionId={}, status={}", 
                    submission.getId(), JudgeStatus.JUDGING);
            
        } catch (Exception e) {
            log.error("更新提交状态失败: submissionId={}", submission.getId(), e);
            throw e;
        }
    }
}
