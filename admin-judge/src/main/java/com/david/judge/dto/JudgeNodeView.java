package com.david.judge.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record JudgeNodeView(
        Long id,
        String name,
        String status,
        Map<String, Object> runtimeInfo,
        LocalDateTime lastHeartbeat,
        LocalDateTime createdAt,
        NodeMetrics metrics) {}
