package com.david.problem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DifficultyCreateRequest(
        @NotNull(message = "难度ID不能为空")
                @Min(value = 1, message = "难度ID必须大于等于1")
                @Max(value = 127, message = "难度ID不能超过127")
                Integer id,
        @NotBlank(message = "难度编码不能为空")
                @Size(max = 20, message = "难度编码不能超过20个字符")
                @Pattern(
                        regexp = "^[a-z0-9-]+$",
                        message = "难度编码仅能包含小写字母、数字或短横线")
                String code,
        @NotNull(message = "排序键不能为空")
                @Min(value = 0, message = "排序键不能小于0")
                @Max(value = 255, message = "排序键不能超过255")
                Integer sortKey) {}
