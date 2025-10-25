package com.david.judge.dto;

import java.time.LocalDateTime;

public record SubmissionArtifactView(
        Long id,
        Long submissionId,
        String kind,
        Long fileId,
        String storageKey,
        String sha256,
        String mimeType,
        Long sizeBytes,
        LocalDateTime createdAt) {}
