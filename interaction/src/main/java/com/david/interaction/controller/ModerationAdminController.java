package com.david.interaction.controller;

import com.david.common.forward.CurrentForwardedUser;
import com.david.common.forward.ForwardedUser;
import com.david.common.http.ApiResponse;
import com.david.interaction.dto.ModerationAssignRequest;
import com.david.interaction.dto.ModerationDecisionRequest;
import com.david.interaction.dto.ModerationTaskDetailView;
import com.david.interaction.dto.ModerationTaskQuery;
import com.david.interaction.dto.ModerationTaskSummaryView;
import com.david.interaction.dto.PageResult;
import com.david.interaction.service.ModerationAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/interaction/moderation/tasks")
public class ModerationAdminController {

    private final ModerationAdminService moderationAdminService;

    @GetMapping
    public ApiResponse<PageResult<ModerationTaskSummaryView>> listTasks(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String source) {
        ModerationTaskQuery query =
                new ModerationTaskQuery(page, size, status, entityType, reviewerId, riskLevel, source);
        log.info("查询审核任务: {}", query);
        PageResult<ModerationTaskSummaryView> result =
                moderationAdminService.listTasks(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/{taskId}")
    public ApiResponse<ModerationTaskDetailView> getTask(@PathVariable Long taskId) {
        log.info("获取审核任务详情: {}", taskId);
        ModerationTaskDetailView detail = moderationAdminService.getTaskDetail(taskId);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{taskId}/assign")
    public ApiResponse<ModerationTaskDetailView> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ModerationAssignRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info(
                "指派审核任务 taskId={}, reviewer={}, operator={}",
                taskId,
                request.reviewerId(),
                operator != null ? operator.id() : null);
        ModerationTaskDetailView detail =
                moderationAdminService.assignTask(taskId, request, operator);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{taskId}/take")
    public ApiResponse<ModerationTaskDetailView> takeTask(
            @PathVariable Long taskId, @CurrentForwardedUser ForwardedUser operator) {
        log.info("认领审核任务 taskId={}, operator={}", taskId, operator != null ? operator.id() : null);
        ModerationTaskDetailView detail =
                moderationAdminService.takeTask(taskId, operator);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{taskId}/decision")
    public ApiResponse<ModerationTaskDetailView> makeDecision(
            @PathVariable Long taskId,
            @Valid @RequestBody ModerationDecisionRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info(
                "处理审核任务 taskId={}, decision={}, operator={}",
                taskId,
                request.decision(),
                operator != null ? operator.id() : null);
        ModerationTaskDetailView detail =
                moderationAdminService.decide(taskId, request, operator);
        return ApiResponse.success(detail);
    }
}

