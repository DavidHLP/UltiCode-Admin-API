package com.david.contest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ContestProblemUpsertRequest(
        @NotNull(message = "题目ID不能为空") Long problemId,
        @Size(max = 8, message = "题目别名长度不能超过8个字符") String alias,
        @PositiveOrZero(message = "题目分值不能为负数") Integer points,
        @Min(value = 0, message = "题目排序不能小于0") Integer orderNo) {}
