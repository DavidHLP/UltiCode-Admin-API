package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.core.exception.BusinessException;
import com.david.interaction.entity.ModerationAction;
import com.david.interaction.entity.ModerationTask;
import com.david.interaction.mapper.ModerationActionMapper;
import com.david.interaction.mapper.ModerationTaskMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ModerationWorkflowService {

    private final ModerationTaskMapper moderationTaskMapper;
    private final ModerationActionMapper moderationActionMapper;
    private final ObjectMapper objectMapper;

    public ModerationWorkflowService(
            ModerationTaskMapper moderationTaskMapper,
            ModerationActionMapper moderationActionMapper,
            ObjectMapper objectMapper) {
        this.moderationTaskMapper = moderationTaskMapper;
        this.moderationActionMapper = moderationActionMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ModerationTask ensurePendingTaskForComment(
            Long commentId, String riskLevel, String source, String notes, List<String> hits) {
        ModerationTask existing = findLatestTaskByCommentId(commentId);
        if (existing != null
                && ("pending".equals(existing.getStatus())
                        || "in_review".equals(existing.getStatus()))) {
            existing.setRiskLevel(riskLevel);
            if (StringUtils.hasText(notes)) {
                existing.setNotes(notes);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setMetadata(buildMetadata(existing.getMetadata(), hits));
            moderationTaskMapper.updateById(existing);
            return existing;
        }

        ModerationTask task = new ModerationTask();
        task.setEntityType("comment");
        task.setEntityId(commentId);
        task.setStatus("pending");
        task.setPriority(determinePriority(riskLevel));
        task.setSource(source);
        task.setRiskLevel(riskLevel);
        task.setNotes(notes);
        task.setMetadata(writeMetadata(hits));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        moderationTaskMapper.insert(task);
        return task;
    }

    private int determinePriority(String riskLevel) {
        if ("high".equalsIgnoreCase(riskLevel)) {
            return 1;
        }
        if ("medium".equalsIgnoreCase(riskLevel)) {
            return 2;
        }
        return 3;
    }

    private String buildMetadata(String existingMetadata, List<String> hits) {
        if (CollectionUtils.isEmpty(hits)) {
            return existingMetadata;
        }
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.hasText(existingMetadata)) {
            try {
                metadata =
                        objectMapper.readValue(
                                existingMetadata,
                                objectMapper
                                        .getTypeFactory()
                                        .constructMapType(Map.class, String.class, Object.class));
            } catch (JsonProcessingException e) {
                log.warn("解析审核任务原始metadata失败: {}", existingMetadata, e);
            }
        }
        metadata.put("hits", hits);
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("序列化审核任务metadata失败", e);
            return existingMetadata;
        }
    }

    private String writeMetadata(List<String> hits) {
        if (CollectionUtils.isEmpty(hits)) {
            return null;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("hits", hits);
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("序列化审核任务metadata失败", e);
            return null;
        }
    }

    public ModerationTask findLatestTaskByCommentId(Long commentId) {
        LambdaQueryWrapper<ModerationTask> wrapper =
                Wrappers.lambdaQuery(ModerationTask.class)
                        .eq(ModerationTask::getEntityType, "comment")
                        .eq(ModerationTask::getEntityId, commentId)
                        .orderByDesc(ModerationTask::getCreatedAt)
                        .last("LIMIT 1");
        return moderationTaskMapper.selectOne(wrapper);
    }

    public Map<Long, ModerationTask> findLatestTasksByCommentIds(List<Long> commentIds) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return Collections.emptyMap();
        }
        List<ModerationTask> tasks =
                moderationTaskMapper.selectList(
                        Wrappers.lambdaQuery(ModerationTask.class)
                                .eq(ModerationTask::getEntityType, "comment")
                                .in(ModerationTask::getEntityId, commentIds)
                                .orderByDesc(ModerationTask::getCreatedAt));
        Map<Long, ModerationTask> result = new HashMap<>();
        for (ModerationTask task : tasks) {
            result.putIfAbsent(task.getEntityId(), task);
        }
        return result;
    }

    public ModerationTask loadTaskOrThrow(Long taskId) {
        ModerationTask task = moderationTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "审核任务不存在");
        }
        return task;
    }

    public List<ModerationAction> listActionsByTaskId(Long taskId) {
        return moderationActionMapper.selectList(
                Wrappers.lambdaQuery(ModerationAction.class)
                        .eq(ModerationAction::getTaskId, taskId)
                        .orderByAsc(ModerationAction::getCreatedAt));
    }

    @Transactional
    public void logAction(
            Long taskId,
            String action,
            Long operatorId,
            String remarks,
            Map<String, Object> context) {
        ModerationAction record = new ModerationAction();
        record.setTaskId(taskId);
        record.setAction(action);
        record.setOperatorId(operatorId);
        record.setRemarks(remarks);
        record.setCreatedAt(LocalDateTime.now());
        if (context != null && !context.isEmpty()) {
            try {
                record.setContext(objectMapper.writeValueAsString(context));
            } catch (JsonProcessingException e) {
                log.warn("序列化审核操作上下文失败", e);
            }
        }
        moderationActionMapper.insert(record);
    }

    @Transactional
    public void updateTask(ModerationTask task) {
        task.setUpdatedAt(LocalDateTime.now());
        moderationTaskMapper.updateById(task);
    }

    @Transactional
    public void updateTaskStatus(
            Long taskId, String status, Long reviewerId, String riskLevel, String notes) {
        ModerationTask task = loadTaskOrThrow(taskId);
        task.setStatus(status);
        task.setReviewerId(reviewerId);
        task.setRiskLevel(riskLevel);
        if (StringUtils.hasText(notes)) {
            task.setNotes(notes);
        }
        task.setUpdatedAt(LocalDateTime.now());
        if ("approved".equals(status) || "rejected".equals(status)) {
            task.setReviewedAt(LocalDateTime.now());
        }
        moderationTaskMapper.updateById(task);
    }

    public Map<String, Object> parseContext(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper
                            .getTypeFactory()
                            .constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException e) {
            log.warn("解析审核上下文失败: {}", json, e);
            return Collections.emptyMap();
        }
    }
}
