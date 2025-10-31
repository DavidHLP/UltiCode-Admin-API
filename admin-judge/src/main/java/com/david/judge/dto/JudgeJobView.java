package com.david.judge.dto;

import java.time.LocalDateTime;

public record JudgeJobView(
        Long id,
        Long submissionId,
        String status,
        Integer priority,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        NodeSummary node,
        SubmissionSummary submission,
        boolean hasArtifacts,
        TestSummary testSummary) {}
