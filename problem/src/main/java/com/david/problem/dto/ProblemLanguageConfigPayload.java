package com.david.problem.dto;

import jakarta.validation.constraints.NotNull;

public record ProblemLanguageConfigPayload(
        @NotNull(message = "语言ID不能为空") Integer languageId,
        String functionName,
        String starterCode) {}
