package com.david.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TagCreateRequest(
        @NotBlank(message = "标签别名不能为空")
                @Size(max = 100, message = "标签别名不能超过100个字符")
                @Pattern(
                        regexp = "^[a-z0-9-]+$",
                        message = "标签别名仅能包含小写字母、数字或短横线")
                String slug,
        @NotBlank(message = "标签名称不能为空")
                @Size(max = 100, message = "标签名称不能超过100个字符")
                String name) {}
