package com.david.problem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record ProblemUpsertRequest(
        @NotBlank(message = "题目别名不能为空") String slug,
        @NotBlank(message = "题目类型不能为空") String problemType,
        @NotNull(message = "难度不能为空") Integer difficultyId,
        Integer categoryId,
        Long creatorId,
        String solutionEntry,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        Boolean isPublic,
        Map<String, Object> meta,
        @Valid @NotNull(message = "题面不能为空")
                @Size(min = 1, message = "至少提供一份题面描述")
                List<ProblemStatementPayload> statements,
        List<Long> tagIds,
        @Valid List<ProblemLanguageConfigPayload> languageConfigs) {}
