package com.david.contest.controller;

import com.david.common.forward.CurrentForwardedUser;
import com.david.common.forward.ForwardedUser;
import com.david.common.http.ApiResponse;
import com.david.contest.dto.ContestDetailView;
import com.david.contest.dto.ContestOptionsResponse;
import com.david.contest.dto.ContestParticipantView;
import com.david.contest.dto.ContestParticipantsUpsertRequest;
import com.david.contest.dto.ContestProblemView;
import com.david.contest.dto.ContestProblemsUpsertRequest;
import com.david.contest.dto.ContestSummaryView;
import com.david.contest.dto.ContestUpsertRequest;
import com.david.contest.dto.PageResult;
import com.david.contest.service.ContestPlanningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/contests")
@RequiredArgsConstructor
public class ContestAdminController {

    private final ContestPlanningService contestPlanningService;

    @GetMapping
    public ApiResponse<PageResult<ContestSummaryView>> listContests(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startFrom,
            @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime endTo) {
        log.info(
                "查询比赛列表 page={}, size={}, keyword={}, kind={}, status={}, visible={}, startFrom={}, endTo={}",
                page,
                size,
                keyword,
                kind,
                status,
                visible,
                startFrom,
                endTo);
        PageResult<ContestSummaryView> result =
                contestPlanningService.listContests(
                        page, size, keyword, kind, status, visible, startFrom, endTo);
        return ApiResponse.success(result);
    }

    @GetMapping("/{contestId}")
    public ApiResponse<ContestDetailView> getContest(@PathVariable Long contestId) {
        log.info("获取比赛详情 contestId={}", contestId);
        ContestDetailView detail = contestPlanningService.getContest(contestId);
        return ApiResponse.success(detail);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ContestDetailView> createContest(
            @Valid @RequestBody ContestUpsertRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        ContestUpsertRequest sanitized = enrichCreator(request, operator);
        log.info(
                "创建比赛 title={}, kind={}, operator={}",
                sanitized.title(),
                sanitized.kind(),
                operator != null ? operator.id() : null);
        ContestDetailView detail = contestPlanningService.createContest(sanitized);
        return ApiResponse.success(detail);
    }

    @PutMapping("/{contestId}")
    public ApiResponse<ContestDetailView> updateContest(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestUpsertRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        ContestUpsertRequest sanitized = enrichCreator(request, operator);
        log.info(
                "更新比赛 contestId={}, operator={}",
                contestId,
                operator != null ? operator.id() : null);
        ContestDetailView detail = contestPlanningService.updateContest(contestId, sanitized);
        return ApiResponse.success(detail);
    }

    @DeleteMapping("/{contestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContest(@PathVariable Long contestId) {
        log.info("删除比赛 contestId={}", contestId);
        contestPlanningService.deleteContest(contestId);
    }

    @PutMapping("/{contestId}/problems")
    public ApiResponse<List<ContestProblemView>> replaceContestProblems(
            @PathVariable Long contestId, @Valid @RequestBody ContestProblemsUpsertRequest request) {
        log.info("更新比赛题目 contestId={}, size={}", contestId, request.problems().size());
        List<ContestProblemView> views = contestPlanningService.replaceContestProblems(contestId, request);
        return ApiResponse.success(views);
    }

    @DeleteMapping("/{contestId}/problems/{problemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeContestProblem(@PathVariable Long contestId, @PathVariable Long problemId) {
        log.info("移除比赛题目 contestId={}, problemId={}", contestId, problemId);
        contestPlanningService.removeContestProblem(contestId, problemId);
    }

    @PostMapping("/{contestId}/participants")
    public ApiResponse<List<ContestParticipantView>> addParticipants(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestParticipantsUpsertRequest request) {
        log.info("批量新增参赛者 contestId={}, size={}", contestId, request.userIds().size());
        List<ContestParticipantView> participants =
                contestPlanningService.addParticipants(contestId, request);
        return ApiResponse.success(participants);
    }

    @DeleteMapping("/{contestId}/participants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipant(@PathVariable Long contestId, @PathVariable Long userId) {
        log.info("移除参赛者 contestId={}, userId={}", contestId, userId);
        contestPlanningService.removeParticipant(contestId, userId);
    }

    @GetMapping("/options")
    public ApiResponse<ContestOptionsResponse> loadOptions() {
        log.info("加载比赛选项数据");
        ContestOptionsResponse options = contestPlanningService.loadOptions();
        return ApiResponse.success(options);
    }

    private ContestUpsertRequest enrichCreator(
            ContestUpsertRequest request, ForwardedUser operator) {
        Long creatorId = operator != null ? operator.id() : null;
        return new ContestUpsertRequest(
                request.title(),
                request.descriptionMd(),
                request.kind(),
                request.startTime(),
                request.endTime(),
                request.visible(),
                creatorId);
    }
}
