package com.david.contest.dto;

import com.david.contest.enums.ContestRegistrationSource;

import jakarta.validation.constraints.NotNull;

public record ContestRegistrationCreateRequest(
        @NotNull(message = "用户ID不能为空") Long userId,
        ContestRegistrationSource source,
        String note) {}
