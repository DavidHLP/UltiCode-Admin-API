package com.david.judge.dto;

public record SubmissionTestView(
        Long id,
        Long testcaseId,
        Long groupId,
        String groupName,
        boolean sampleGroup,
        Integer orderIndex,
        String verdict,
        Integer timeMs,
        Integer memoryKb,
        Integer score,
        String message) {}
