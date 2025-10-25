package com.david.judge.dto;

public record JudgeJobQuery(
        int page,
        int size,
        String status,
        Long nodeId,
        boolean onlyUnassigned,
        Long submissionId,
        String keyword) {}
