package com.david.problem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DifficultyUpdateRequest(
        @Size(max = 20, message = "难度编码不能超过20个字符")
                @Pattern(
                        regexp = "^[a-z0-9-]+$",
                        message = "难度编码仅能包含小写字母、数字或短横线")
                String code,
        @Min(value = 0, message = "排序键不能小于0")
                @Max(value = 255, message = "排序键不能超过255")
                Integer sortKey) {}
