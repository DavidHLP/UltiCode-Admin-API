package com.david.contest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ContestUpsertRequest(
        @NotBlank(message = "比赛标题不能为空") String title,
        String descriptionMd,
        @NotBlank(message = "比赛类型不能为空") String kind,
        @NotNull(message = "开始时间不能为空") LocalDateTime startTime,
        @NotNull(message = "结束时间不能为空") LocalDateTime endTime,
        Boolean visible,
        Long createdBy) {}
