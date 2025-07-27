package com.david.dto;

import com.david.judge.enums.LanguageType;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 提交代码请求DTO
 */
@Data
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
