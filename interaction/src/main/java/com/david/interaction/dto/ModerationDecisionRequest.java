package com.david.interaction.dto;

import jakarta.validation.constraints.NotBlank;

public record ModerationDecisionRequest(
        @NotBlank(message = "决策不能为空") String decision,
        String notes,
        String moderationLevel) {}

