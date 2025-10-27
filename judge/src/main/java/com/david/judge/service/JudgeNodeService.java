package com.david.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.judge.dto.JudgeNodeView;
import com.david.judge.dto.NodeMetrics;
import com.david.judge.entity.JudgeNode;
import com.david.judge.mapper.JudgeJobMapper;
import com.david.judge.mapper.JudgeNodeMapper;
import com.david.judge.mapper.model.NodeFinishedAggregate;
import com.david.judge.mapper.model.NodeStatusAggregate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeNodeService {

    private final JudgeNodeMapper judgeNodeMapper;
    private final JudgeJobMapper judgeJobMapper;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public List<JudgeNodeView> listNodes(String status, String keyword) {
        LambdaQueryWrapper<JudgeNode> query = Wrappers.lambdaQuery(JudgeNode.class);
        if (StringUtils.hasText(status)) {
            query.eq(JudgeNode::getStatus, status.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            query.like(JudgeNode::getName, trimmed);
        }
        query.orderByAsc(JudgeNode::getName);
        List<JudgeNode> nodes = judgeNodeMapper.selectList(query);
        if (nodes.isEmpty()) {
            return List.of();
        }
        Map<Long, NodeMetrics> metrics = buildMetrics(nodes);
        return nodes.stream().map(node -> toView(node, metrics.get(node.getId()))).toList();
    }

    private Map<Long, NodeMetrics> buildMetrics(List<JudgeNode> nodes) {
        Set<Long> nodeIds =
                nodes.stream()
                        .map(JudgeNode::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        if (nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, NodeMetricsBuilder> buffers = new HashMap<>();
        LocalDateTime threshold = LocalDateTime.now(clock).minusHours(1);
        List<NodeStatusAggregate> statusAggregates = judgeJobMapper.aggregateByNodeIds(nodeIds);
        for (NodeStatusAggregate aggregate : statusAggregates) {
            NodeMetricsBuilder builder =
                    buffers.computeIfAbsent(aggregate.nodeId(), id -> new NodeMetricsBuilder());
            switch (aggregate.status()) {
                case "queued" -> builder.queued += safeCount(aggregate.count());
                case "running" -> builder.running += safeCount(aggregate.count());
                case "failed" -> builder.failed += safeCount(aggregate.count());
                default -> {
                    // ignore
                }
            }
        }
        List<NodeFinishedAggregate> finishedAggregates =
                judgeJobMapper.aggregateRecentFinished(nodeIds, threshold);
        for (NodeFinishedAggregate aggregate : finishedAggregates) {
            NodeMetricsBuilder builder =
                    buffers.computeIfAbsent(aggregate.nodeId(), id -> new NodeMetricsBuilder());
            builder.finishedLastHour += safeCount(aggregate.count());
        }
        Map<Long, NodeMetrics> metrics = new HashMap<>();
        for (Long nodeId : nodeIds) {
            NodeMetricsBuilder builder = buffers.getOrDefault(nodeId, new NodeMetricsBuilder());
            metrics.put(
                    nodeId,
                    new NodeMetrics(
                            builder.queued,
                            builder.running,
                            builder.failed,
                            builder.finishedLastHour));
        }
        return metrics;
    }

    private long safeCount(Long value) {
        return value == null ? 0 : value;
    }

    private JudgeNodeView toView(JudgeNode node, NodeMetrics metrics) {
        Map<String, Object> runtime = parseRuntimeInfo(node.getRuntimeInfo());
        NodeMetrics resolved = metrics == null ? new NodeMetrics(0, 0, 0, 0) : metrics;
        return new JudgeNodeView(
                node.getId(),
                node.getName(),
                node.getStatus(),
                runtime,
                node.getLastHeartbeat(),
                node.getCreatedAt(),
                resolved);
    }

    private Map<String, Object> parseRuntimeInfo(String payload) {
        if (!StringUtils.hasText(payload)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(
                    payload, objectMapper.getTypeFactory().constructType(Map.class));
        } catch (JsonProcessingException ex) {
            log.warn("解析 runtime_info 失败: {}", payload, ex);
            return Map.of("raw", payload);
        }
    }

    private static final class NodeMetricsBuilder {
        long queued;
        long running;
        long failed;
        long finishedLastHour;
    }
}
