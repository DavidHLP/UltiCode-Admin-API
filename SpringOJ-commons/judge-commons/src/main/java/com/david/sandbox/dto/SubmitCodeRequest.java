package com.david.sandbox.dto;

import com.david.judge.enums.LanguageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交代码请求DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitCodeRequest {
    /** 题目ID */
    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    /** 编程语言 */
    @NotNull(message = "编程语言不能为空")
    private LanguageType language;

    /** 源代码 */
    @NotBlank(message = "源代码不能为空")
    private String sourceCode;
}
