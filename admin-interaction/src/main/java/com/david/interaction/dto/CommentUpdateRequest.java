package com.david.interaction.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
        @NotBlank(message = "评论内容不能为空") String contentMd,
        String contentRendered,
        String visibility) {}

