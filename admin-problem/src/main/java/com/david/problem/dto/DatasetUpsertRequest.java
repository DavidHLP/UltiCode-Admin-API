package com.david.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatasetUpsertRequest(
        @NotBlank(message = "数据集名称不能为空") String name,
        @NotNull(message = "请指定是否激活该数据集") Boolean isActive,
        @NotBlank(message = "校验器类型不能为空") String checkerType,
        Long checkerFileId,
        Double floatAbsTol,
        Double floatRelTol) {}
