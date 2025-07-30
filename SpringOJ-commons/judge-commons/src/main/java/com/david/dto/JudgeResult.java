package com.david.dto;

import java.util.List;

import com.david.judge.enums.JudgeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题结果DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgeResult {
    /** 提交ID */
    private Long submissionId;
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
}
