package com.david.problem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProblemReviewDecisionRequest(
        @NotNull(message = "请明确审核结果") Boolean approved,
        Long reviewerId,
        @Size(max = 500, message = "审核备注不能超过500个字符") String notes) {}
