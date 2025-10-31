package com.david.contest.dto;

import com.david.contest.enums.ContestRegistrationStatus;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ContestRegistrationDecisionRequest(
        @NotEmpty(message = "请选择需要处理的报名记录") List<Long> registrationIds,
        ContestRegistrationStatus targetStatus,
        String note) {}
