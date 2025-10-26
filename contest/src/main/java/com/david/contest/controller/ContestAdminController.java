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
import com.david.contest.dto.ContestRegistrationCreateRequest;
import com.david.contest.dto.ContestRegistrationDecisionRequest;
import com.david.contest.dto.ContestRegistrationView;
import com.david.contest.dto.ContestSummaryView;
import com.david.contest.dto.ContestUpsertRequest;
import com.david.contest.dto.PageResult;
import com.david.contest.dto.ProblemSummaryOption;
import com.david.contest.dto.UserSummaryOption;
import com.david.contest.enums.ContestRegistrationStatus;
import com.david.common.http.exception.BusinessException;
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
import org.springframework.util.StringUtils;

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
            @Valid @RequestBody ContestParticipantsUpsertRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info(
                "批量新增参赛者 contestId={}, size={}, operator={}",
                contestId,
                request.userIds().size(),
                operator != null ? operator.id() : null);
        List<ContestParticipantView> participants =
                contestPlanningService.addParticipants(
                        contestId, request, operator != null ? operator.id() : null);
        return ApiResponse.success(participants);
    }

    @DeleteMapping("/{contestId}/participants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipant(
            @PathVariable Long contestId,
            @PathVariable Long userId,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info(
                "移除参赛者 contestId={}, userId={}, operator={}",
                contestId,
                userId,
                operator != null ? operator.id() : null);
        contestPlanningService.removeParticipant(contestId, userId, operator != null ? operator.id() : null);
    }

    @GetMapping("/{contestId}/registrations")
    public ApiResponse<PageResult<ContestRegistrationView>> listRegistrations(
            @PathVariable Long contestId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "20")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 200, message = "分页大小不能超过200")
                    int size,
            @RequestParam(required = false) String status) {
        ContestRegistrationStatus filterStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                filterStatus = ContestRegistrationStatus.fromCode(status);
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "无效的报名状态");
            }
        }
        log.info(
                "查询报名列表 contestId={}, page={}, size={}, status={}",
                contestId,
                page,
                size,
                status);
        PageResult<ContestRegistrationView> result =
                contestPlanningService.listRegistrations(contestId, filterStatus, page, size);
        return ApiResponse.success(result);
    }

    @PostMapping("/{contestId}/registrations")
    public ApiResponse<ContestRegistrationView> createRegistration(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestRegistrationCreateRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        ContestRegistrationCreateRequest sanitized =
                new ContestRegistrationCreateRequest(
                        request.userId(),
                        request.source() != null
                                ? request.source()
                                : com.david.contest.enums.ContestRegistrationSource.ADMIN,
                        request.note());
        log.info(
                "创建报名记录 contestId={}, userId={}, operator={}",
                contestId,
                sanitized.userId(),
                operator != null ? operator.id() : null);
        ContestRegistrationView view =
                contestPlanningService.createRegistration(
                        contestId, sanitized, operator != null ? operator.id() : null);
        return ApiResponse.success(view);
    }

    @PostMapping("/{contestId}/registrations/decision")
    public ApiResponse<List<ContestRegistrationView>> decideRegistrations(
            @PathVariable Long contestId,
            @Valid @RequestBody ContestRegistrationDecisionRequest request,
            @CurrentForwardedUser ForwardedUser operator) {
        log.info(
                "处理报名审批 contestId={}, size={}, target={}, operator={}",
                contestId,
                request.registrationIds().size(),
                request.targetStatus(),
                operator != null ? operator.id() : null);
        List<ContestRegistrationView> result =
                contestPlanningService.decideRegistrations(
                        contestId, request, operator != null ? operator.id() : null);
        return ApiResponse.success(result);
    }

    @GetMapping("/options")
    public ApiResponse<ContestOptionsResponse> loadOptions() {
        log.info("加载比赛选项数据");
        ContestOptionsResponse options = contestPlanningService.loadOptions();
        return ApiResponse.success(options);
    }

    @GetMapping("/problem-search")
    public ApiResponse<List<ProblemSummaryOption>> searchProblems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        log.info("搜索题目 keyword={}, limit={}", keyword, limit);
        return ApiResponse.success(contestPlanningService.searchProblems(keyword, limit));
    }

    @GetMapping("/user-search")
    public ApiResponse<List<UserSummaryOption>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        log.info("搜索用户 keyword={}, limit={}", keyword, limit);
        return ApiResponse.success(contestPlanningService.searchUsers(keyword, limit));
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
                creatorId,
                request.registrationMode(),
                request.registrationStartTime(),
                request.registrationEndTime(),
                request.maxParticipants(),
                request.penaltyPerWrong(),
                request.scoreboardFreezeMinutes(),
                request.hideScoreDuringFreeze());
    }
}
