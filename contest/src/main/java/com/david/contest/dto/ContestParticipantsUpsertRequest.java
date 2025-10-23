package com.david.contest.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ContestParticipantsUpsertRequest(
        @NotEmpty(message = "请至少选择一名参赛者") List<Long> userIds) {}
