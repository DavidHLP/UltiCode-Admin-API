package com.david.chain.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.david.chain.AbstractJudgeChainHandler;
import com.david.dto.JudgeContext;
import com.david.dto.SandboxExecuteRequest;
import com.david.strategy.impl.JudgeStrategyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求构建处理器 - 负责构建和定制沙箱执行请求
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestBuildHandler extends AbstractJudgeChainHandler {
    
    private final JudgeStrategyFactory strategyFactory;
    
    @Override
    protected void doHandle(JudgeContext context) {
        var submission = context.getSubmission();
        log.info("开始构建沙箱请求: submissionId={}", submission.getId());
        
        // 构建基础沙箱请求
        SandboxExecuteRequest sandboxRequest = buildBasicRequest(context);
        
        // 使用语言策略定制请求
        sandboxRequest = customizeRequestByLanguage(sandboxRequest, submission.getLanguage());
        
        context.setSandboxRequest(sandboxRequest);
        
        log.info("沙箱请求构建完成: submissionId={}, language={}, timeLimit={}ms, memoryLimit={}MB",
                submission.getId(), submission.getLanguage(), 
                sandboxRequest.getTimeLimit(), sandboxRequest.getMemoryLimit());
    }
    
    /**
     * 构建基础沙箱请求
     */
    private SandboxExecuteRequest buildBasicRequest(JudgeContext context) {
        var submission = context.getSubmission();
        var problem = context.getProblem();
        var testCases = context.getTestCases();
        var codeTemplate = context.getCodeTemplate();
        
        var request = new SandboxExecuteRequest();
        request.setSourceCode(submission.getSourceCode());
        request.setLanguage(submission.getLanguage());
        request.setTimeLimit(problem.getTimeLimit());
        request.setMemoryLimit(problem.getMemoryLimit());
        request.setSubmissionId(submission.getId());
        request.setMainWrapperTemplate(codeTemplate.getMainWrapperTemplate());
        
        // 提取测试用例输入和期望输出
        List<String> inputs = extractTestCaseInputs(testCases);
        List<String> expectedOutputs = extractTestCaseOutputs(testCases);
        
        request.setInputs(inputs);
        request.setExpectedOutputs(expectedOutputs);
        
        log.debug("基础沙箱请求构建完成: submissionId={}, testCaseCount={}", 
                 submission.getId(), testCases.size());
        
        return request;
    }
    
    /**
     * 使用语言策略定制请求
     */
    private SandboxExecuteRequest customizeRequestByLanguage(SandboxExecuteRequest request, 
                                                           com.david.judge.enums.LanguageType language) {
        var strategy = strategyFactory.getStrategy(language);
        var customizedRequest = strategy.customizeRequest(request);
        
        log.debug("语言策略定制完成: language={}, timeLimit={}ms, memoryLimit={}MB",
                 language, customizedRequest.getTimeLimit(), customizedRequest.getMemoryLimit());
        
        return customizedRequest;
    }
    
    /**
     * 提取测试用例输入
     */
    private List<String> extractTestCaseInputs(List<com.david.judge.TestCase> testCases) {
//        return testCases.stream()
//                .map(testCase -> testCase.getInputs().stream()
//                        .map(input -> String.join("\n", input.getInput()))
//                        .collect(Collectors.joining("\n")))
//                .toList();
	    return null;
    }
    
    /**
     * 提取测试用例期望输出
     */
    private List<String> extractTestCaseOutputs(List<com.david.judge.TestCase> testCases) {
//        return testCases.stream()
//                .map(com.david.judge.TestCase::getOutput)
//                .toList();
	    return null;
    }
}
