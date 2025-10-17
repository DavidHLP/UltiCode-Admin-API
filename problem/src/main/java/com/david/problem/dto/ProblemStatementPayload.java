package com.david.problem.dto;

import jakarta.validation.constraints.NotBlank;

public record ProblemStatementPayload(
        @NotBlank(message = "语言代码不能为空") String langCode,
        @NotBlank(message = "标题不能为空") String title,
        @NotBlank(message = "题面描述不能为空") String descriptionMd,
        String constraintsMd,
        String examplesMd) {}
