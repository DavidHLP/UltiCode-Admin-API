package com.david.problem.controller;

import com.david.core.http.ApiResponse;
import com.david.problem.dto.PageResult;
import com.david.problem.dto.ProblemDetailView;
import com.david.problem.dto.ProblemOptionsResponse;
import com.david.problem.dto.ProblemPublishRequest;
import com.david.problem.dto.ProblemReviewDecisionRequest;
import com.david.problem.dto.ProblemSubmitReviewRequest;
import com.david.problem.dto.ProblemSummaryView;
import com.david.problem.dto.ProblemUpsertRequest;
import com.david.problem.service.ProblemManagementService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/problems")
@RequiredArgsConstructor
public class ProblemAdminController {

    private final ProblemManagementService problemManagementService;

    @GetMapping
    public ApiResponse<PageResult<ProblemSummaryView>> listProblems(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String problemType,
            @RequestParam(required = false) Integer difficultyId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String lifecycleStatus,
            @RequestParam(required = false) String reviewStatus,
            @RequestParam(required = false) String langCode) {
        log.info(
                "查询题目列表，页码: {}, 大小: {}, 关键字: {}, 类型: {}, 难度: {}, 分类: {}, 是否公开: {}, 生命周期: {}, 审核状态: {}, 语言: {}",
                page,
                size,
                keyword,
                problemType,
                difficultyId,
                categoryId,
                isPublic,
                lifecycleStatus,
                reviewStatus,
                langCode);
        PageResult<ProblemSummaryView> result =
                problemManagementService.listProblems(
                        page,
                        size,
                        keyword,
                        problemType,
                        difficultyId,
                        categoryId,
                        isPublic,
                        lifecycleStatus,
                        reviewStatus,
                        langCode);
        return ApiResponse.success(result);
    }

    @GetMapping("/{problemId}")
    public ApiResponse<ProblemDetailView> getProblem(
            @PathVariable Long problemId, @RequestParam(required = false) String langCode) {
        log.info("获取题目详情，问题ID: {}, 语言: {}", problemId, langCode);
        ProblemDetailView detail = problemManagementService.getProblem(problemId, langCode);
        return ApiResponse.success(detail);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProblemDetailView> createProblem(
            @Valid @RequestBody ProblemUpsertRequest request) {
        log.info("创建题目，请求参数: {}", request);
        ProblemDetailView detail = problemManagementService.createProblem(request);
        log.info("创建题目成功，ID: {}", detail.id());
        return ApiResponse.success(detail);
    }

    @PutMapping("/{problemId}")
    public ApiResponse<ProblemDetailView> updateProblem(
            @PathVariable Long problemId, @Valid @RequestBody ProblemUpsertRequest request) {
        log.info("更新题目，ID: {}, 请求参数: {}", problemId, request);
        ProblemDetailView detail = problemManagementService.updateProblem(problemId, request);
        log.info("更新题目成功，ID: {}", detail.id());
        return ApiResponse.success(detail);
    }

    @PostMapping("/{problemId}/submit-review")
    public ApiResponse<ProblemDetailView> submitForReview(
            @PathVariable Long problemId,
            @Valid @RequestBody(required = false) ProblemSubmitReviewRequest request) {
        log.info("题目 {} 提交审核，请求: {}", problemId, request);
        ProblemDetailView detail =
                problemManagementService.submitForReview(problemId, request);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{problemId}/review")
    public ApiResponse<ProblemDetailView> reviewProblem(
            @PathVariable Long problemId, @Valid @RequestBody ProblemReviewDecisionRequest request) {
        log.info("处理题目 {} 的审核结果，请求: {}", problemId, request);
        ProblemDetailView detail = problemManagementService.reviewProblem(problemId, request);
        return ApiResponse.success(detail);
    }

    @PostMapping("/{problemId}/publish")
    public ApiResponse<ProblemDetailView> togglePublish(
            @PathVariable Long problemId, @Valid @RequestBody ProblemPublishRequest request) {
        log.info("更新题目 {} 的发布状态，请求: {}", problemId, request);
        ProblemDetailView detail =
                problemManagementService.togglePublish(problemId, request.publish(), request.notes());
        return ApiResponse.success(detail);
    }

    @GetMapping("/options")
    public ApiResponse<ProblemOptionsResponse> loadOptions() {
        log.info("加载题目字典选项");
        ProblemOptionsResponse response = problemManagementService.loadOptions();
        return ApiResponse.success(response);
    }
}
