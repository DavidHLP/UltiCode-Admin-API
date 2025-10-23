package com.david.contest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContestScoreboardView(
        Long contestId,
        String kind,
        LocalDateTime generatedAt,
        List<ContestScoreboardProblemView> problems,
        List<ContestScoreboardParticipantView> participants) {}
