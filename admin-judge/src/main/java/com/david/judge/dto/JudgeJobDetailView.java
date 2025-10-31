package com.david.judge.dto;

import java.util.List;

public record JudgeJobDetailView(
        JudgeJobView job, List<SubmissionTestView> tests, List<SubmissionArtifactView> artifacts) {}
