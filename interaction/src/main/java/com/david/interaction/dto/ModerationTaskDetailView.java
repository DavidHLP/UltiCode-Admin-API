package com.david.interaction.dto;

import java.util.List;

public record ModerationTaskDetailView(
        ModerationTaskSummaryView task,
        CommentDetailView comment,
        List<ModerationActionView> actions) {}

