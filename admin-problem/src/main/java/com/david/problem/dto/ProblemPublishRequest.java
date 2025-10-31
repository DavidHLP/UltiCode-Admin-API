package com.david.problem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProblemPublishRequest(
        @NotNull(message = "请指定发布状态") Boolean publish,
        Long operatorId,
        @Size(max = 500, message = "备注不能超过500个字符") String notes) {}
