package com.david.problem.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagUpdateRequest(
        @Size(max = 100, message = "标签别名不能超过100个字符")
                @Pattern(
                        regexp = "^[a-z0-9-]+$",
                        message = "标签别名仅能包含小写字母、数字或短横线")
                String slug,
        @Size(max = 100, message = "标签名称不能超过100个字符") String name) {}
