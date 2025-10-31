package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.core.exception.BusinessException;
import com.david.core.forward.ForwardedUser;
import com.david.interaction.dto.ModerationActionView;
import com.david.interaction.dto.ModerationAssignRequest;
import com.david.interaction.dto.ModerationDecisionRequest;
import com.david.interaction.dto.ModerationTaskDetailView;
import com.david.interaction.dto.ModerationTaskQuery;
import com.david.interaction.dto.ModerationTaskSummaryView;
import com.david.interaction.dto.PageResult;
import com.david.interaction.entity.Comment;
import com.david.interaction.entity.ModerationAction;
import com.david.interaction.entity.ModerationTask;
import com.david.interaction.mapper.CommentMapper;
import com.david.interaction.mapper.ModerationTaskMapper;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ModerationAdminService {

    private final ModerationTaskMapper moderationTaskMapper;
    private final CommentMapper commentMapper;
    private final CommentAdminService commentAdminService;
    private final ModerationWorkflowService moderationWorkflowService;

    public ModerationAdminService(
            ModerationTaskMapper moderationTaskMapper,
            CommentMapper commentMapper,
            CommentAdminService commentAdminService,
            ModerationWorkflowService moderationWorkflowService) {
        this.moderationTaskMapper = moderationTaskMapper;
        this.commentMapper = commentMapper;
        this.commentAdminService = commentAdminService;
        this.moderationWorkflowService = moderationWorkflowService;
    }

    public PageResult<ModerationTaskSummaryView> listTasks(ModerationTaskQuery query) {
        int page = query.page() == null || query.page() < 1 ? 1 : query.page();
        int size = query.size() == null || query.size() < 1 ? 10 : query.size();
        Page<ModerationTask> pager = new Page<>(page, size);

        LambdaQueryWrapper<ModerationTask> wrapper = Wrappers.lambdaQuery(ModerationTask.class);
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(ModerationTask::getStatus, query.status().trim());
        }
        if (StringUtils.hasText(query.entityType())) {
            wrapper.eq(ModerationTask::getEntityType, query.entityType().trim());
        }
        if (query.reviewerId() != null) {
            wrapper.eq(ModerationTask::getReviewerId, query.reviewerId());
        }
        if (StringUtils.hasText(query.riskLevel())) {
            wrapper.eq(ModerationTask::getRiskLevel, query.riskLevel().trim());
        }
        if (StringUtils.hasText(query.source())) {
            wrapper.eq(ModerationTask::getSource, query.source().trim());
        }
        wrapper.orderByDesc(ModerationTask::getCreatedAt);

        Page<ModerationTask> result = moderationTaskMapper.selectPage(pager, wrapper);
        List<ModerationTaskSummaryView> items =
                result.getRecords().stream().map(this::toSummary).toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public ModerationTaskDetailView getTaskDetail(Long taskId) {
        ModerationTask task = moderationWorkflowService.loadTaskOrThrow(taskId);
        var commentDetail = commentAdminService.getCommentDetail(task.getEntityId());
        List<ModerationAction> actions = moderationWorkflowService.listActionsByTaskId(taskId);
        List<ModerationActionView> actionViews =
                actions.stream()
                        .map(
                                action ->
                                        new ModerationActionView(
                                                action.getId(),
                                                action.getTaskId(),
                                                action.getAction(),
                                                action.getOperatorId(),
                                                action.getRemarks(),
                                                moderationWorkflowService.parseContext(
                                                        action.getContext()),
                                                action.getCreatedAt()))
                        .toList();
        return new ModerationTaskDetailView(toSummary(task), commentDetail, actionViews);
    }

    @Transactional
    public ModerationTaskDetailView assignTask(
            Long taskId, ModerationAssignRequest request, ForwardedUser operator) {
        ModerationTask task = moderationWorkflowService.loadTaskOrThrow(taskId);
        Long reviewerId =
                request != null && request.reviewerId() != null
                        ? request.reviewerId()
                        : operator != null ? operator.id() : null;
        task.setReviewerId(reviewerId);
        task.setStatus("in_review");
        if (request != null && StringUtils.hasText(request.notes())) {
            task.setNotes(request.notes().trim());
        }
        task.setUpdatedAt(LocalDateTime.now());
        moderationWorkflowService.updateTask(task);
        if (reviewerId != null) {
            moderationWorkflowService.logAction(
                    taskId,
                    "assigned",
                    operator != null ? operator.id() : null,
                    request != null ? request.notes() : "任务指派",
                    Map.of("reviewerId", reviewerId));
        }
        return getTaskDetail(taskId);
    }

    @Transactional
    public ModerationTaskDetailView takeTask(Long taskId, ForwardedUser operator) {
        return assignTask(taskId, new ModerationAssignRequest(null, "认领任务"), operator);
    }

    @Transactional
    public ModerationTaskDetailView decide(
            Long taskId, ModerationDecisionRequest request, ForwardedUser operator) {
        ModerationTask task = moderationWorkflowService.loadTaskOrThrow(taskId);
        Comment comment = commentMapper.selectById(task.getEntityId());
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在，无法审核");
        }
        String decision = request.decision().trim().toLowerCase(Locale.ROOT);
        String commentStatus = mapCommentStatus(decision);
        String taskStatus = mapTaskDecision(decision);
        if (commentStatus == null || taskStatus == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的审核操作");
        }

        comment.setStatus(commentStatus);
        if (StringUtils.hasText(request.notes())) {
            comment.setModerationNotes(request.notes().trim());
        }
        if (StringUtils.hasText(request.moderationLevel())) {
            comment.setModerationLevel(request.moderationLevel().trim());
        }
        comment.setLastModeratedBy(operator != null ? operator.id() : null);
        comment.setLastModeratedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        if ("approved".equals(commentStatus)) {
            comment.setSensitiveFlag(Boolean.FALSE);
        }
        commentMapper.updateById(comment);

        Long reviewerId =
                task.getReviewerId() != null
                        ? task.getReviewerId()
                        : operator != null ? operator.id() : null;
        moderationWorkflowService.updateTaskStatus(
                taskId, taskStatus, reviewerId, comment.getModerationLevel(), request.notes());
        moderationWorkflowService.logAction(
                taskId,
                taskStatus,
                operator != null ? operator.id() : null,
                request.notes(),
                Map.of(
                        "decision", decision,
                        "commentStatus", commentStatus));

        if ("escalated".equals(taskStatus)) {
            moderationWorkflowService.ensurePendingTaskForComment(
                    comment.getId(),
                    comment.getModerationLevel(),
                    "escalated",
                    "任务升级复审",
                    commentAdminService.getCommentDetail(comment.getId()).sensitiveHits());
        }
        return getTaskDetail(taskId);
    }

    private String mapCommentStatus(String decision) {
        return switch (decision) {
            case "approve" -> "approved";
            case "reject" -> "rejected";
            case "escalate" -> "pending";
            default -> null;
        };
    }

    private String mapTaskDecision(String decision) {
        return switch (decision) {
            case "approve" -> "approved";
            case "reject" -> "rejected";
            case "escalate" -> "escalated";
            default -> null;
        };
    }

    private ModerationTaskSummaryView toSummary(ModerationTask task) {
        return new ModerationTaskSummaryView(
                task.getId(),
                task.getEntityType(),
                task.getEntityId(),
                task.getStatus(),
                task.getPriority(),
                task.getSource(),
                task.getRiskLevel(),
                task.getReviewerId(),
                task.getNotes(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getReviewedAt());
    }
}
