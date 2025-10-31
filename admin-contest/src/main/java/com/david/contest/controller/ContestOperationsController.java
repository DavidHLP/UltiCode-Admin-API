package com.david.contest.controller;

import com.david.contest.dto.ContestScoreboardView;
import com.david.contest.dto.ContestSubmissionView;
import com.david.contest.dto.PageResult;
import com.david.contest.service.ContestOperationsService;
import com.david.core.http.ApiResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/contests")
public class ContestOperationsController {

    private final ContestOperationsService contestOperationsService;

    @GetMapping("/{contestId}/scoreboard")
    public ApiResponse<ContestScoreboardView> generateScoreboard(@PathVariable Long contestId) {
        log.info("生成赛事榜单 contestId={}", contestId);
        ContestScoreboardView view = contestOperationsService.generateScoreboard(contestId);
        return ApiResponse.success(view);
    }

    @GetMapping("/{contestId}/submissions")
    public ApiResponse<PageResult<ContestSubmissionView>> listSubmissions(
            @PathVariable Long contestId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 200, message = "分页大小不能超过200")
                    int size,
            @RequestParam(required = false) String verdict,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long problemId) {
        log.info(
                "查询比赛提交 contestId={}, page={}, size={}, verdict={}, userId={}, problemId={}",
                contestId,
                page,
                size,
                verdict,
                userId,
                problemId);
        PageResult<ContestSubmissionView> submissions =
                contestOperationsService.listSubmissions(contestId, page, size, verdict, userId, problemId);
        return ApiResponse.success(submissions);
    }
}
