package com.david.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SensitiveWordUpsertRequest(
        @NotBlank(message = "敏感词不能为空") @Size(max = 100, message = "敏感词长度不能超过100")
                String word,
        @Size(max = 64, message = "分类长度不能超过64") String category,
        @NotBlank(message = "处理等级不能为空") String level,
        @Size(max = 100, message = "替换词长度不能超过100") String replacement,
        @Size(max = 255, message = "描述长度不能超过255") String description,
        Boolean active) {}

