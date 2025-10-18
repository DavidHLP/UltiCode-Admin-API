package com.david.problem.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(max = 50, message = "分类编码不能超过50个字符")
                @Pattern(
                        regexp = "^[a-z0-9-]+$",
                        message = "分类编码仅能包含小写字母、数字或短横线")
                String code,
        @Size(max = 100, message = "分类名称不能超过100个字符") String name) {}
