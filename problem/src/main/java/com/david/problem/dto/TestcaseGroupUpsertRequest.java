package com.david.problem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestcaseGroupUpsertRequest(
        @NotBlank(message = "测试组名称不能为空") String name,
        @NotNull(message = "请指定是否为样例组") Boolean isSample,
        @NotNull(message = "测试组权重不能为空") @Min(value = 1, message = "测试组权重至少为1") Integer weight) {}
