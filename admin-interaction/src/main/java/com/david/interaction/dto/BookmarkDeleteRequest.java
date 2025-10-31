package com.david.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookmarkDeleteRequest(
        @NotNull(message = "用户ID不能为空") Long userId,
        @NotBlank(message = "实体类型不能为空") String entityType,
        @NotNull(message = "实体ID不能为空") Long entityId) {}

