package com.david.interaction.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentStatusUpdateRequest(
        @NotBlank(message = "状态不能为空") String status,
        String moderationNotes,
        String moderationLevel) {}

