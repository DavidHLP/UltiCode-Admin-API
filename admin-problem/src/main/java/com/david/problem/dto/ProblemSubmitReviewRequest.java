package com.david.problem.dto;

import jakarta.validation.constraints.Size;

public record ProblemSubmitReviewRequest(
        Long operatorId,
        @Size(max = 500, message = "备注不能超过500个字符") String notes) {}
