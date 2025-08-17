package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.enums.JudgeStatus;
import com.david.testcase.TestCase;
import com.david.utils.java.JavaCodeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Java 代码格式化处理器
 * 
 * 功能：
 * 1. 基于新的 JSON 系统生成完整的可运行代码
 * 2. 将 Solution 类与 Main 类合并
 * 3. 严格验证所有输入输出为 JSON 格式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaFormatCodeHandler extends Handler {
    
    private final JavaCodeUtils javaCodeUtils;
    
    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        try {
            log.info("开始 Java 代码格式化处理，提交ID: {}", judgmentContext.getSubmissionId());
            
            // 验证必要参数
            if (!validateContext(judgmentContext)) {
                return false;
            }
            
            // 生成主程序代码
            String mainClass = generateMainClass(judgmentContext);
            
            // 将用户方法包装成完整的 Solution 类
            String solutionClass = wrapUserCodeInSolutionClass(judgmentContext);
            
            // 合并 Solution 和 Main 类
            String completeCode = javaCodeUtils.generateMainFixSolutionClass(
                mainClass, 
                solutionClass
            );
            
            // 设置生成的可运行代码
            judgmentContext.setRunCode(completeCode);
            judgmentContext.setJudgeStatus(JudgeStatus.CONTINUE);
            
            log.info("Java 代码格式化完成，提交ID: {}", judgmentContext.getSubmissionId());
            return nextHandler.handleRequest(judgmentContext);
            
        } catch (Exception e) {
            log.error("Java 代码格式化失败，提交ID: {}, 错误: {}", 
                     judgmentContext.getSubmissionId(), e.getMessage(), e);
            
            judgmentContext.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            judgmentContext.setCompileInfo("代码格式化失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证判题上下文的必要参数
     */
    private boolean validateContext(JudgmentContext context) {
        if (context.getSolutionCode() == null || context.getSolutionCode().trim().isEmpty()) {
            log.error("Solution 代码为空，提交ID: {}", context.getSubmissionId());
            context.setJudgeStatus(JudgeStatus.COMPILE_ERROR);
            context.setCompileInfo("Solution 代码不能为空");
            return false;
        }
        
        if (context.getSolutionFunctionName() == null || context.getSolutionFunctionName().trim().isEmpty()) {
            log.error("解题函数名为空，提交ID: {}", context.getSubmissionId());
            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setCompileInfo("解题函数名不能为空");
            return false;
        }
        
        if (context.getTestCases() == null || context.getTestCases().isEmpty()) {
            log.error("测试用例为空，提交ID: {}", context.getSubmissionId());
            context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);
            context.setCompileInfo("测试用例不能为空");
            return false;
        }
        
        return true;
    }
    
    /**
     * 生成包含所有测试用例的主程序代码
     */
    private String generateMainClass(JudgmentContext context) {
        List<TestCase> testCases = context.getTestCases();
        
        // 构建测试用例数据结构
        List<List<String>> allInputTypes = testCases.stream()
            .map(tc -> tc.getTestCaseInput().stream()
                       .map(input -> input.getInputType())
                       .collect(Collectors.toList()))
            .collect(Collectors.toList());
        
        List<List<String>> allInputValues = testCases.stream()
            .map(tc -> tc.getTestCaseInput().stream()
                       .map(input -> validateJsonInput(input.getInputContent()))
                       .collect(Collectors.toList()))
            .collect(Collectors.toList());
        
        List<String> allExpectedOutputs = testCases.stream()
            .map(tc -> validateJsonInput(tc.getTestCaseOutput().getOutput()))
            .collect(Collectors.toList());
        
        // 确定输出类型
        String outputType = testCases.get(0).getTestCaseOutput().getOutputType();
        
        return javaCodeUtils.generateMultiTestCaseMainClass(
            context.getSolutionFunctionName(),
            outputType,
            allInputTypes,
            allInputValues,
            allExpectedOutputs
        );
    }
    
    /**
     * 验证输入是否为合法的 JSON 格式
     */
    private String validateJsonInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("测试用例输入不能为空");
        }
        
        String trimmed = input.trim();
        
        // 基本 JSON 格式验证
        if (!isValidJsonFormat(trimmed)) {
            throw new IllegalArgumentException("测试用例输入必须是合法的 JSON 格式: " + trimmed);
        }
        
        return trimmed;
    }
    
    /**
     * 简单的 JSON 格式验证
     */
    private boolean isValidJsonFormat(String input) {
        // 基础 JSON 格式检查
        if (input.startsWith("\"") && input.endsWith("\"")) return true; // 字符串
        if (input.equals("true") || input.equals("false")) return true; // 布尔值
        if (input.matches("-?\\d+(\\.\\d+)?")) return true; // 数字
        if (input.startsWith("[") && input.endsWith("]")) return true; // 数组
        if (input.startsWith("{") && input.endsWith("}")) return true; // 对象
        if (input.equals("null")) return true; // null值
        
        return false;
    }
    
    /**
     * 将用户代码包装成完整的 Solution 类
     */
    private String wrapUserCodeInSolutionClass(JudgmentContext context) {
        String userCode = context.getSolutionCode();
        
        // 检查用户代码是否已经包含完整的类定义
        if (userCode.contains("class Solution")) {
            return userCode;
        }
        
        // 检查是否需要 List 相关的导入
        boolean needsUtilImport = userCode.contains("List<") || 
                                 userCode.contains("ArrayList") || 
                                 userCode.contains("HashMap") || 
                                 userCode.contains("Arrays.asList");
        
        StringBuilder solutionClass = new StringBuilder();
        
        if (needsUtilImport) {
            solutionClass.append("import java.util.*;\n\n");
        }
        
        solutionClass.append("class Solution {\n")
                    .append("    ")
                    .append(userCode.replaceAll("\\n", "\n    ")) // 添加缩进
                    .append("\n")
                    .append("}\n");
        
        return solutionClass.toString();
    }
}
