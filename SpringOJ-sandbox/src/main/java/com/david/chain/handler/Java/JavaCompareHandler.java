package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.JudgmentResult;
import com.david.enums.JudgeStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Java 代码比较处理器
 * 负责比较用户程序的运行结果与期望输出，基于严格的 JSON 格式比较
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JavaCompareHandler extends Handler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        try {
            log.info("开始比较运行结果与期望输出");
            
            // 验证输入参数
            if (!validateInput(judgmentContext)) {
                return false;
            }
            
            List<JudgmentResult> results = judgmentContext.getJudgmentResults();
            
            // 对每个测试用例进行比较
            boolean allCorrect = true;
            for (JudgmentResult result : results) {
                boolean isCorrect = compareResult(result, judgmentContext);
                if (!isCorrect) {
                    allCorrect = false;
                }
            }
            
            // 设置最终判题状态
            if (allCorrect) {
                judgmentContext.setJudgeStatus(JudgeStatus.ACCEPTED);
                log.info("所有测试用例通过，判题结果: ACCEPTED");
            } else {
                judgmentContext.setJudgeStatus(JudgeStatus.WRONG_ANSWER);
                log.info("存在错误答案，判题结果: WRONG_ANSWER");
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("比较过程中发生异常", e);
            judgmentContext.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            judgmentContext.setJudgeInfo("比较异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证输入参数
     */
    private boolean validateInput(JudgmentContext context) {
        if (context.getJudgmentResults() == null || context.getJudgmentResults().isEmpty()) {
            log.error("没有找到运行结果");
            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setJudgeInfo("没有找到运行结果");
            return false;
        }
        
        if (context.getTestCases() == null || context.getTestCases().isEmpty()) {
            log.error("没有找到测试用例");
            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setJudgeInfo("没有找到测试用例");
            return false;
        }
        
        return true;
    }
    
    /**
     * 比较单个测试用例的结果
     * 根据测试用例ID匹配对应的期望输出
     */
    private boolean compareResult(JudgmentResult result, JudgmentContext context) {
        try {
            // 查找对应的测试用例
            Long testCaseId = result.getTestCaseId();
            String expectedOutput = findExpectedOutput(testCaseId, context.getTestCases());
            
            if (expectedOutput == null) {
                log.error("找不到测试用例ID {} 对应的期望输出", testCaseId);
                result.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
                result.setJudgeInfo("找不到对应的期望输出");
                return false;
            }
            
            // 从 compileInfo 中提取实际输出 (由 JavaRunHandler 设置)
            String actualOutput = extractActualOutput(result.getCompileInfo());
            
            log.debug("比较测试用例 {} - 实际输出: {}, 期望输出: {}", testCaseId, actualOutput, expectedOutput);
            
            // 使用严格的 JSON 比较
            boolean isEqual = compareJsonStrings(actualOutput, expectedOutput);
            
            if (isEqual) {
                result.setJudgeStatus(JudgeStatus.ACCEPTED);
                result.setJudgeInfo("答案正确");
                log.debug("测试用例 {} 通过", testCaseId);
            } else {
                result.setJudgeStatus(JudgeStatus.WRONG_ANSWER);
                result.setJudgeInfo(String.format("答案错误 - 期望: %s, 实际: %s", expectedOutput, actualOutput));
                log.debug("测试用例 {} 不通过 - 期望: {}, 实际: {}", testCaseId, expectedOutput, actualOutput);
                
                // 设置上下文中的错误信息（只记录第一个错误）
                if (context.getErrorTestCaseId() == null) {
                    context.setErrorTestCaseId(testCaseId);
                    context.setErrorTestCaseOutput(actualOutput);
                    context.setErrorTestCaseExpectOutput(expectedOutput);
                }
            }
            
            return isEqual;
            
        } catch (Exception e) {
            log.error("比较测试用例时发生异常", e);
            result.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            result.setJudgeInfo("比较异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 严格的 JSON 字符串比较
     * 使用 Jackson 解析后进行语义比较，避免格式差异
     */
    private boolean compareJsonStrings(String actual, String expected) {
        if (actual == null && expected == null) {
            return true;
        }
        
        if (actual == null || expected == null) {
            return false;
        }
        
        try {
            // 解析为 JsonNode 进行语义比较
            JsonNode actualNode = objectMapper.readTree(actual);
            JsonNode expectedNode = objectMapper.readTree(expected);
            
            return actualNode.equals(expectedNode);
            
        } catch (JsonProcessingException e) {
            log.warn("JSON 解析失败，使用字符串直接比较 - actual: {}, expected: {}", actual, expected, e);
            // 如果 JSON 解析失败，回退到字符串比较
            return actual.trim().equals(expected.trim());
        }
    }
    
    /**
     * 根据测试用例ID查找期望输出
     */
    private String findExpectedOutput(Long testCaseId, List<com.david.testcase.TestCase> testCases) {
        if (testCaseId == null || testCases == null) {
            return null;
        }
        
        for (com.david.testcase.TestCase testCase : testCases) {
            if (testCaseId.equals(testCase.getId())) {
                return testCase.getTestCaseOutput() != null ? 
                    testCase.getTestCaseOutput().getOutput() : null;
            }
        }
        
        return null;
    }
    
    /**
     * 从 compileInfo 中提取实际输出
     * JavaRunHandler 将输出存储在 compileInfo 字段中，格式为 "实际输出: {actual_output}"
     */
    private String extractActualOutput(String compileInfo) {
        if (compileInfo == null) {
            return null;
        }
        
        // JavaRunHandler 存储格式是: "实际输出: {actual_output}"
        if (compileInfo.startsWith("实际输出: ")) {
            return compileInfo.substring(5).trim();
        }
        
        // 否则直接返回 compileInfo
        return compileInfo;
    }
}
