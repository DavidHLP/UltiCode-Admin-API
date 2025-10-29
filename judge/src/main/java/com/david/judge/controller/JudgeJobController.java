package com.david.judge.controller;

import com.david.core.security.CurrentForwardedUser;
import com.david.core.forward.ForwardedUser;
import com.david.core.http.ApiResponse;
import com.david.judge.dto.JudgeJobDetailView;
import com.david.judge.dto.JudgeJobQuery;
import com.david.judge.dto.JudgeJobView;
import com.david.judge.dto.PageResult;
import com.david.judge.service.JudgeJobService;
import com.david.judge.service.SensitiveOperationGuard;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('platform_admin')")
@RequestMapping("/api/admin/judge/jobs")
public class JudgeJobController {

    private final JudgeJobService judgeJobService;
    private final SensitiveOperationGuard sensitiveOperationGuard;

    @GetMapping
    public ApiResponse<PageResult<JudgeJobView>> pageJobs(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long nodeId,
            @RequestParam(defaultValue = "false") boolean onlyUnassigned,
            @RequestParam(required = false) Long submissionId,
            @RequestParam(required = false) String keyword) {
        JudgeJobQuery query = new JudgeJobQuery(page, size, status, nodeId, onlyUnassigned, submissionId, keyword);
        PageResult<JudgeJobView> result = judgeJobService.pageJobs(query);
        return ApiResponse.success(result);
    }

    @GetMapping("/{jobId}")
    public ApiResponse<JudgeJobDetailView> getJobDetail(@PathVariable Long jobId) {
        return ApiResponse.success(judgeJobService.getJobDetail(jobId));
    }

    @PostMapping("/{jobId}/retry")
    public ApiResponse<Void> retryJob(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long jobId) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        judgeJobService.retryJob(jobId);
        return ApiResponse.success(null);
    }
}
