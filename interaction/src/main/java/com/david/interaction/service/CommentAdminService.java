package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.common.forward.ForwardedUser;
import com.david.interaction.dto.CommentDetailView;
import com.david.interaction.dto.CommentQuery;
import com.david.interaction.dto.CommentStatusUpdateRequest;
import com.david.interaction.dto.CommentSummaryView;
import com.david.interaction.dto.CommentUpdateRequest;
import com.david.interaction.dto.ModerationTaskSummaryView;
import com.david.interaction.dto.PageResult;
import com.david.interaction.entity.Comment;
import com.david.interaction.entity.ModerationTask;
import com.david.interaction.exception.BusinessException;
import com.david.interaction.mapper.CommentMapper;
import com.david.interaction.mapper.ReactionMapper;
import com.david.interaction.mapper.result.ReactionAggregationRow;
import com.david.interaction.service.model.SensitiveWordAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class CommentAdminService {

    private final CommentMapper commentMapper;
    private final ReactionMapper reactionMapper;
    private final SensitiveWordAdminService sensitiveWordAdminService;
    private final ModerationWorkflowService moderationWorkflowService;
    private final ObjectMapper objectMapper;

    public CommentAdminService(
            CommentMapper commentMapper,
            ReactionMapper reactionMapper,
            SensitiveWordAdminService sensitiveWordAdminService,
            ModerationWorkflowService moderationWorkflowService,
            ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.reactionMapper = reactionMapper;
        this.sensitiveWordAdminService = sensitiveWordAdminService;
        this.moderationWorkflowService = moderationWorkflowService;
        this.objectMapper = objectMapper;
    }

    public PageResult<CommentSummaryView> listComments(CommentQuery query) {
        int page = query.page() == null || query.page() < 1 ? 1 : query.page();
        int size = query.size() == null || query.size() < 1 ? 10 : query.size();
        Page<Comment> pager = new Page<>(page, size);

        LambdaQueryWrapper<Comment> wrapper = Wrappers.lambdaQuery(Comment.class);
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(Comment::getStatus, query.status().trim());
        }
        if (StringUtils.hasText(query.entityType())) {
            wrapper.eq(Comment::getEntityType, query.entityType().trim());
        }
        if (query.entityId() != null) {
            wrapper.eq(Comment::getEntityId, query.entityId());
        }
        if (query.userId() != null) {
            wrapper.eq(Comment::getUserId, query.userId());
        }
        if (Boolean.TRUE.equals(query.sensitiveOnly())) {
            wrapper.eq(Comment::getSensitiveFlag, Boolean.TRUE);
        }
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            wrapper.and(
                    w ->
                            w.like(Comment::getContentMd, keyword)
                                    .or()
                                    .like(Comment::getModerationNotes, keyword));
        }
        if (StringUtils.hasText(query.moderationLevel())) {
            wrapper.eq(Comment::getModerationLevel, query.moderationLevel().trim());
        }
        wrapper.orderByDesc(Comment::getCreatedAt);

        Page<Comment> result = commentMapper.selectPage(pager, wrapper);
        List<Comment> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(List.of(), result.getTotal(), result.getCurrent(), result.getSize());
        }

        List<Long> commentIds = records.stream().map(Comment::getId).toList();
        Map<Long, Map<String, Long>> reactionSummary = loadReactionSummary(commentIds);
        Map<Long, ModerationTask> taskMap =
                moderationWorkflowService.findLatestTasksByCommentIds(commentIds);

        List<CommentSummaryView> views =
                records.stream()
                        .map(
                                comment ->
                                        toSummaryView(
                                                comment,
                                                reactionSummary.getOrDefault(comment.getId(), Map.of()),
                                                taskMap.get(comment.getId())))
                        .toList();
        return new PageResult<>(
                views, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public CommentDetailView getCommentDetail(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        Map<Long, Map<String, Long>> reactionSummary =
                loadReactionSummary(List.of(commentId));
        ModerationTask task = moderationWorkflowService.findLatestTaskByCommentId(commentId);
        return toDetailView(
                comment,
                reactionSummary.getOrDefault(commentId, Map.of()),
                task);
    }

    @Transactional
    public CommentDetailView updateComment(
            Long commentId, CommentUpdateRequest request, ForwardedUser operator) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        comment.setContentMd(request.contentMd().trim());
        comment.setContentRendered(
                StringUtils.hasText(request.contentRendered())
                        ? request.contentRendered().trim()
                        : null);
        if (StringUtils.hasText(request.visibility())) {
            comment.setVisibility(request.visibility().trim());
        }
        comment.setUpdatedAt(LocalDateTime.now());

        SensitiveWordAnalysisResult analysis =
                sensitiveWordAdminService.analyzeContent(comment.getContentMd());
        applyAnalysis(comment, analysis);
        commentMapper.updateById(comment);

        if (analysis.blocked() || analysis.needReview()) {
            ModerationTask task =
                    moderationWorkflowService.ensurePendingTaskForComment(
                            commentId,
                            analysis.riskLevel(),
                            "auto",
                            "内容更新触发审核",
                            analysis.hits());
            moderationWorkflowService.logAction(
                    task.getId(),
                    "created",
                    operator != null ? operator.id() : null,
                    "评论内容更新后进入审核",
                    Map.of("commentId", commentId));
        }
        return getCommentDetail(commentId);
    }

    @Transactional
    public CommentDetailView updateCommentStatus(
            Long commentId,
            CommentStatusUpdateRequest request,
            ForwardedUser operator) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "评论不存在");
        }

        String normalizedStatus = request.status().trim().toLowerCase(Locale.ROOT);
        comment.setStatus(normalizedStatus);
        if (StringUtils.hasText(request.moderationNotes())) {
            comment.setModerationNotes(request.moderationNotes().trim());
        }
        if (StringUtils.hasText(request.moderationLevel())) {
            comment.setModerationLevel(request.moderationLevel().trim());
        }
        comment.setLastModeratedBy(operator != null ? operator.id() : null);
        comment.setLastModeratedAt(LocalDateTime.now());
        if ("approved".equals(normalizedStatus)) {
            comment.setSensitiveFlag(Boolean.FALSE);
        }
        commentMapper.updateById(comment);

        ModerationTask task =
                moderationWorkflowService.findLatestTaskByCommentId(commentId);
        if (task != null) {
            String taskStatus = mapTaskStatus(normalizedStatus);
            moderationWorkflowService.updateTaskStatus(
                    task.getId(),
                    taskStatus,
                    operator != null ? operator.id() : null,
                    comment.getModerationLevel(),
                    request.moderationNotes());
            moderationWorkflowService.logAction(
                    task.getId(),
                    taskStatus,
                    operator != null ? operator.id() : null,
                    request.moderationNotes(),
                    Map.of("commentStatus", normalizedStatus));
        } else if ("pending".equals(normalizedStatus)) {
            ModerationTask newTask =
                    moderationWorkflowService.ensurePendingTaskForComment(
                            commentId,
                            comment.getModerationLevel(),
                            "manual",
                            request.moderationNotes(),
                            readHits(comment.getSensitiveHits()));
            moderationWorkflowService.logAction(
                    newTask.getId(),
                    "created",
                    operator != null ? operator.id() : null,
                    "手动发起审核",
                    Map.of("commentStatus", normalizedStatus));
        }

        return getCommentDetail(commentId);
    }

    private void applyAnalysis(Comment comment, SensitiveWordAnalysisResult analysis) {
        comment.setSensitiveFlag(analysis.hasSensitive());
        comment.setModerationLevel(analysis.riskLevel());
        comment.setSensitiveHits(writeHits(analysis.hits()));
        if (analysis.blocked()) {
            comment.setStatus("hidden");
        } else if (analysis.needReview() && !"hidden".equals(comment.getStatus())) {
            comment.setStatus("pending");
        }
    }

    private String mapTaskStatus(String commentStatus) {
        return switch (commentStatus) {
            case "approved" -> "approved";
            case "rejected", "hidden" -> "rejected";
            case "pending" -> "pending";
            default -> "pending";
        };
    }

    private Map<Long, Map<String, Long>> loadReactionSummary(List<Long> commentIds) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return Map.of();
        }
        List<ReactionAggregationRow> rows =
                reactionMapper.aggregateByEntity("comment", commentIds);
        Map<Long, Map<String, Long>> summary = new HashMap<>();
        for (ReactionAggregationRow row : rows) {
            summary.computeIfAbsent(row.getEntityId(), id -> new HashMap<>())
                    .put(row.getKind(), row.getTotal());
        }
        return summary;
    }

    private CommentSummaryView toSummaryView(
            Comment comment, Map<String, Long> reactionStats, ModerationTask task) {
        return new CommentSummaryView(
                comment.getId(),
                comment.getEntityType(),
                comment.getEntityId(),
                comment.getUserId(),
                comment.getParentId(),
                comment.getStatus(),
                comment.getVisibility(),
                buildPreview(comment.getContentMd()),
                comment.getSensitiveFlag(),
                comment.getModerationLevel(),
                comment.getModerationNotes(),
                comment.getLastModeratedBy(),
                comment.getLastModeratedAt(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                reactionStats,
                toTaskSummary(task));
    }

    private CommentDetailView toDetailView(
            Comment comment, Map<String, Long> reactionStats, ModerationTask task) {
        return new CommentDetailView(
                comment.getId(),
                comment.getEntityType(),
                comment.getEntityId(),
                comment.getUserId(),
                comment.getParentId(),
                comment.getStatus(),
                comment.getVisibility(),
                comment.getContentMd(),
                comment.getContentRendered(),
                comment.getSensitiveFlag(),
                readHits(comment.getSensitiveHits()),
                comment.getModerationLevel(),
                comment.getModerationNotes(),
                comment.getLastModeratedBy(),
                comment.getLastModeratedAt(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                reactionStats,
                toTaskSummary(task));
    }

    private ModerationTaskSummaryView toTaskSummary(ModerationTask task) {
        if (task == null) {
            return null;
        }
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

    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String normalized = content.strip();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120) + "...";
    }

    private List<String> readHits(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("解析敏感词命中记录失败: {}", json, e);
            return List.of();
        }
    }

    private String writeHits(List<String> hits) {
        if (CollectionUtils.isEmpty(hits)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(hits);
        } catch (JsonProcessingException e) {
            log.warn("序列化敏感词命中记录失败", e);
            return null;
        }
    }
}
