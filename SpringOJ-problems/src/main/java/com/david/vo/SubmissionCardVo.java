package com.david.vo;

import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionCardVo {
    private Long id;

    /** 提交用户的ID */
    private Long userId;

    /** 题目的ID */
    private Long problemId;

    /** 编程语言 */
    private LanguageType language;

    /** 判题状态 */
    private JudgeStatus status;

    /** 程序执行耗时，单位为毫秒 */
    private Integer timeUsed;

    /** 程序执行内存消耗，单位为KB */
    private Integer memoryUsed;
}
