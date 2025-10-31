package com.david.problem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TestcaseUpsertRequest(
        @NotNull(message = "用例序号不能为空") @Min(value = 0, message = "用例序号不能为负数") Integer orderIndex,
        Long inputFileId,
        Long outputFileId,
        String inputJson,
        String outputJson,
        String outputType,
        @NotNull(message = "请填写用例分值") @Min(value = 0, message = "用例分值不能为负数") Integer score) {}
