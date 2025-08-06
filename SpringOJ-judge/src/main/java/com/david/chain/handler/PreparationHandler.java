package com.david.chain.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.david.chain.AbstractJudgeChainHandler;
import com.david.constants.JudgeConstants;
import com.david.dto.JudgeContext;
import com.david.interfaces.ProblemServiceFeignClient;
import com.david.judge.CodeTemplate;
import com.david.judge.Problem;
import com.david.judge.TestCase;
import com.david.utils.ResponseValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 准备处理器 - 负责获取判题所需的题目信息、测试用例和代码模板
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreparationHandler extends AbstractJudgeChainHandler {
    
    private final ProblemServiceFeignClient problemServiceFeignClient;
    
    @Override
    protected void doHandle(JudgeContext context) {
        var submission = context.getSubmission();
        log.info("开始准备判题数据: submissionId={}, problemId={}", 
                submission.getId(), submission.getProblemId());
        
        // 获取题目信息
        Problem problem = fetchProblem(submission.getProblemId());
        context.setProblem(problem);
        
        // 获取测试用例
        List<TestCase> testCases = fetchTestCases(problem.getId());
        context.setTestCases(testCases);
        
        // 获取代码模板
        CodeTemplate codeTemplate = fetchCodeTemplate(problem.getId(), submission.getLanguage().getName());
        context.setCodeTemplate(codeTemplate);
        
        log.info("判题数据准备完成: submissionId={}, testCaseCount={}", 
                submission.getId(), testCases.size());
    }
    
    /**
     * 获取题目信息
     */
    private Problem fetchProblem(Long problemId) {
        log.debug("获取题目信息: problemId={}", problemId);
        
        var response = problemServiceFeignClient.getProblemById(problemId);
        var problem = ResponseValidator.getValidatedData(
            response, 
            String.format(JudgeConstants.ErrorMessages.PROBLEM_NOT_FOUND, problemId)
        );
        
        log.debug("题目信息获取成功: problemId={}, title={}", problemId, problem.getTitle());
        return problem;
    }
    
    /**
     * 获取测试用例
     */
    private List<TestCase> fetchTestCases(Long problemId) {
        log.debug("获取测试用例: problemId={}", problemId);
        
        var response = problemServiceFeignClient.getTestCasesByProblemId(problemId);
        ResponseValidator.validateListNotEmpty(
            response, 
            String.format(JudgeConstants.ErrorMessages.TEST_CASES_NOT_FOUND, problemId)
        );
        
        var testCases = response.getData();
        log.debug("测试用例获取成功: problemId={}, count={}", problemId, testCases.size());
        return testCases;
    }
    
    /**
     * 获取代码模板
     */
    private CodeTemplate fetchCodeTemplate(Long problemId, String languageName) {
        log.debug("获取代码模板: problemId={}, language={}", problemId, languageName);
        
        var response = problemServiceFeignClient.getCodeTemplateByProblemIdAndLanguage(problemId, languageName);
        var codeTemplate = ResponseValidator.getValidatedData(
            response, 
            String.format(JudgeConstants.ErrorMessages.CODE_TEMPLATE_NOT_FOUND, problemId)
        );
        
        log.debug("代码模板获取成功: problemId={}, language={}", problemId, languageName);
        return codeTemplate;
    }
}
