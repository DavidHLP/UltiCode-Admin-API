package com.david.problem.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LanguageUpdateRequest(
        @Size(max = 40, message = "语言编码不能超过40个字符")
                @Pattern(
                        regexp = "^[a-z0-9.+-]+$",
                        message = "语言编码仅能包含小写字母、数字、点、加号或短横线")
                String code,
        @Size(max = 80, message = "展示名称不能超过80个字符") String displayName,
        @Size(max = 255, message = "运行镜像不能超过255个字符") String runtimeImage,
        Boolean isActive) {}
