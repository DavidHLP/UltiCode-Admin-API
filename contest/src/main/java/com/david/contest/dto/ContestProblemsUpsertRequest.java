package com.david.contest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ContestProblemsUpsertRequest(
        @NotEmpty(message = "请至少选择一道题目")
                List<@Valid ContestProblemUpsertRequest> problems) {}
