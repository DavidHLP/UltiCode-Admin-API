package com.david.interaction.controller;

import com.david.common.http.ApiResponse;
import com.david.interaction.dto.PageResult;
import com.david.interaction.dto.ReactionDeleteRequest;
import com.david.interaction.dto.ReactionQuery;
import com.david.interaction.dto.ReactionView;
import com.david.interaction.service.ReactionAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/interaction/reactions")
public class ReactionAdminController {

    private final ReactionAdminService reactionAdminService;

    @GetMapping
    public ApiResponse<PageResult<ReactionView>> listReactions(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String source) {
        ReactionQuery query = new ReactionQuery(page, size, userId, entityType, entityId, kind, source);
        log.info("查询反馈列表: {}", query);
        PageResult<ReactionView> result = reactionAdminService.listReactions(query);
        return ApiResponse.success(result);
    }

    @DeleteMapping
    public ApiResponse<Void> deleteReaction(@Valid @RequestBody ReactionDeleteRequest request) {
        log.info(
                "删除反馈 userId={}, entityType={}, entityId={}, kind={}",
                request.userId(),
                request.entityType(),
                request.entityId(),
                request.kind());
        reactionAdminService.deleteReaction(request);
        return ApiResponse.success(null);
    }
}
