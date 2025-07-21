package com.david.dto;

import com.david.judge.enums.JudgeStatus;
import lombok.Data;
import java.util.List;

/**
 * 判题结果DTO
 */
@Data
public class JudgeResult {
    /** 判题状态 */
    private JudgeStatus status;
    
    /** 得分 */
    private Integer score;
    
    /** 运行时间(ms) */
    private Integer timeUsed;
    
    /** 内存使用(KB) */
    private Integer memoryUsed;
    
    /** 编译信息 */
    private String compileInfo;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 测试点详情 */
    private List<TestCaseResult> testCaseResults;
    
    @Data
    public static class TestCaseResult {
        /** 测试点ID */
        private Long testCaseId;
        
        /** 测试点状态 */
        private JudgeStatus status;
        
        /** 运行时间(ms) */
        private Integer timeUsed;
        
        /** 内存使用(KB) */
        private Integer memoryUsed;
        
        /** 得分 */
        private Integer score;
        
        /** 错误信息 */
        private String errorMessage;
    }
}
