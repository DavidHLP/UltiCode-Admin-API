package com.david.dto;

import java.util.List;

import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;
import com.david.judge.CodeTemplate;

import lombok.Builder;
import lombok.Data;

/**
 * 判题上下文 - 在责任链中传递的判题相关数据
 * 
 * @author David
 */
@Data
@Builder
public class JudgeContext {
    
    /** 提交记录 */
    private Submission submission;
    
    /** 题目信息 */
    private Problem problem;
    
    /** 测试用例列表 */
    private List<TestCase> testCases;
    
    /** 代码模板 */
    private CodeTemplate codeTemplate;
    
    /** 沙箱执行请求 */
    private SandboxExecuteRequest sandboxRequest;
    
    /** 判题结果 */
    private JudgeResult judgeResult;
    
    /** 用户ID */
    private Long userId;
    
    /** 是否需要继续处理 */
    @Builder.Default
    private boolean shouldContinue = true;
    
    /** 错误信息 */
    private String errorMessage;
    
    /**
     * 标记处理失败，停止后续处理
     * 
     * @param errorMessage 错误信息
     */
    public void markFailed(String errorMessage) {
        this.shouldContinue = false;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 检查是否应该继续处理
     * 
     * @return true 如果应该继续处理
     */
    public boolean shouldContinueProcessing() {
        return shouldContinue;
    }
}
