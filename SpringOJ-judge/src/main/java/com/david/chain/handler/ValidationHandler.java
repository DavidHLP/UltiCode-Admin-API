package com.david.chain.handler;

import org.springframework.stereotype.Component;

import com.david.chain.AbstractJudgeChainHandler;
import com.david.dto.JudgeContext;
import com.david.exception.JudgeException;
import com.david.strategy.code.impl.JudgeStrategyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 验证处理器 - 负责验证判题请求的基本有效性
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationHandler extends AbstractJudgeChainHandler {
    
    private final JudgeStrategyFactory strategyFactory;
    
    @Override
    protected void doHandle(JudgeContext context) {
        log.info("开始验证判题请求: submissionId={}", context.getSubmission().getId());
        
        validateSubmission(context);
        validateLanguageSupport(context);
        
        log.info("判题请求验证通过: submissionId={}", context.getSubmission().getId());
    }
    
    /**
     * 验证提交记录的基本信息
     */
    private void validateSubmission(JudgeContext context) {
        var submission = context.getSubmission();
        
        if (submission == null) {
            throw new JudgeException("提交记录不能为空");
        }
        
        if (submission.getId() == null) {
            throw new JudgeException("提交ID不能为空");
        }
        
        if (submission.getProblemId() == null) {
            throw new JudgeException("题目ID不能为空", submission.getId());
        }
        
        if (submission.getLanguage() == null) {
            throw new JudgeException("编程语言不能为空", submission.getId());
        }
        
        if (submission.getSourceCode() == null || submission.getSourceCode().trim().isEmpty()) {
            throw new JudgeException("源代码不能为空", submission.getId());
        }
        
        log.debug("提交记录基本信息验证通过: submissionId={}", submission.getId());
    }
    
    /**
     * 验证编程语言支持
     */
    private void validateLanguageSupport(JudgeContext context) {
        var submission = context.getSubmission();
        var language = submission.getLanguage();
        
        if (!strategyFactory.isLanguageSupported(language)) {
            throw JudgeException.unsupportedLanguage(
                language.toString(), 
                submission.getId()
            );
        }
        
        log.debug("编程语言支持验证通过: language={}, submissionId={}", 
                 language, submission.getId());
    }
}
