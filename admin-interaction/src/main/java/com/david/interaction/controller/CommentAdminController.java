package com.david.interaction.controller;

import com.david.core.forward.ForwardedUser;
import com.david.core.http.ApiResponse;
import com.david.core.security.CurrentForwardedUser;
import com.david.interaction.dto.CommentDetailView;
import com.david.interaction.dto.CommentQuery;
import com.david.interaction.dto.CommentStatusUpdateRequest;
import com.david.interaction.dto.CommentSummaryView;
import com.david.interaction.dto.CommentUpdateRequest;
import com.david.interaction.dto.PageResult;
import com.david.interaction.service.CommentAdminService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/interaction/comments")
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @GetMapping
    public ApiResponse<PageResult<CommentSummaryView>> listComments(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean sensitiveOnly,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String moderationLevel) {
        CommentQuery query =
                new CommentQuery(
                        page,
                        size,
                        status,
                        entityType,
                        entityId,
                        userId,
                        sensitiveOnly,
                        keyword,
                        moderationLevel);
        log.info("查询评论列表: {}", query);
        PageResult<CommentSummaryView> result = commentAdminService.listComments(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/{commentId}")
    public ApiResponse<CommentDetailView> getComment(@PathVariable Long commentId) {
        log.info("获取评论详情: {}", commentId);
        CommentDetailView detail = commentAdminService.getCommentDetail(commentId);
        return ApiResponse.success(detail);
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentDetailView> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info("更新评论内容 commentId={}, operator={}", commentId, operator != null ? operator.id() : null);
        CommentDetailView detail =
                commentAdminService.updateComment(commentId, request, operator);
        return ApiResponse.success(detail);
    }

    @PutMapping("/{commentId}/status")
    public ApiResponse<CommentDetailView> updateCommentStatus(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentStatusUpdateRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info(
                "更新评论状态 commentId={}, status={}, operator={}",
                commentId,
                request.status(),
                operator != null ? operator.id() : null);
        CommentDetailView detail =
                commentAdminService.updateCommentStatus(commentId, request, operator);
        return ApiResponse.success(detail);
    }
}

