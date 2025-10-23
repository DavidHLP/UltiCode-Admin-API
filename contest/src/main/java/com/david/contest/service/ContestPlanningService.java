package com.david.contest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.contest.dto.ContestDetailView;
import com.david.contest.dto.ContestKindOption;
import com.david.contest.dto.ContestOptionsResponse;
import com.david.contest.dto.ContestParticipantView;
import com.david.contest.dto.ContestParticipantsUpsertRequest;
import com.david.contest.dto.ContestProblemUpsertRequest;
import com.david.contest.dto.ContestProblemView;
import com.david.contest.dto.ContestProblemsUpsertRequest;
import com.david.contest.dto.ContestSummaryView;
import com.david.contest.dto.ContestUpsertRequest;
import com.david.contest.dto.PageResult;
import com.david.contest.entity.Contest;
import com.david.contest.entity.ContestParticipant;
import com.david.contest.entity.ContestProblem;
import com.david.contest.entity.Problem;
import com.david.contest.entity.ProblemStatement;
import com.david.contest.entity.ProblemStatsView;
import com.david.contest.entity.User;
import com.david.contest.enums.ContestKind;
import com.david.contest.enums.ContestStatus;
import com.david.contest.exception.BusinessException;
import com.david.contest.mapper.ContestMapper;
import com.david.contest.mapper.ContestParticipantMapper;
import com.david.contest.mapper.ContestProblemMapper;
import com.david.contest.mapper.ProblemMapper;
import com.david.contest.mapper.ProblemStatementMapper;
import com.david.contest.mapper.ProblemStatsViewMapper;
import com.david.contest.mapper.SubmissionMapper;
import com.david.contest.mapper.UserMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ContestPlanningService {

    private static final Logger log = LoggerFactory.getLogger(ContestPlanningService.class);
    private static final String DEFAULT_STATEMENT_LANG = "zh-CN";

    private final ContestMapper contestMapper;
    private final ContestProblemMapper contestProblemMapper;
    private final ContestParticipantMapper contestParticipantMapper;
    private final SubmissionMapper submissionMapper;
    private final ProblemMapper problemMapper;
    private final ProblemStatementMapper problemStatementMapper;
    private final ProblemStatsViewMapper problemStatsViewMapper;
    private final UserMapper userMapper;

    public ContestPlanningService(
            ContestMapper contestMapper,
            ContestProblemMapper contestProblemMapper,
            ContestParticipantMapper contestParticipantMapper,
            SubmissionMapper submissionMapper,
            ProblemMapper problemMapper,
            ProblemStatementMapper problemStatementMapper,
            ProblemStatsViewMapper problemStatsViewMapper,
            UserMapper userMapper) {
        this.contestMapper = contestMapper;
        this.contestProblemMapper = contestProblemMapper;
        this.contestParticipantMapper = contestParticipantMapper;
        this.submissionMapper = submissionMapper;
        this.problemMapper = problemMapper;
        this.problemStatementMapper = problemStatementMapper;
        this.problemStatsViewMapper = problemStatsViewMapper;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<ContestSummaryView> listContests(
            int page,
            int size,
            String keyword,
            String kind,
            String status,
            Boolean visible,
            LocalDateTime startFrom,
            LocalDateTime endTo) {
        Page<Contest> pager = new Page<>(page, size);
        LambdaQueryWrapper<Contest> query = Wrappers.lambdaQuery(Contest.class);

        if (StringUtils.hasText(keyword)) {
            query.like(Contest::getTitle, keyword.trim());
        }
        if (StringUtils.hasText(kind)) {
            String normalized = kind.trim().toLowerCase(Locale.ROOT);
            query.eq(Contest::getKind, normalized);
        }
        if (visible != null) {
            query.eq(Contest::getIsVisible, visible ? 1 : 0);
        }
        if (startFrom != null) {
            query.ge(Contest::getStartTime, startFrom);
        }
        if (endTo != null) {
            query.le(Contest::getEndTime, endTo);
        }
        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.hasText(status)) {
            ContestStatus desired =
                    ContestStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            switch (desired) {
                case UPCOMING -> query.gt(Contest::getStartTime, now);
                case RUNNING -> query.le(Contest::getStartTime, now).ge(Contest::getEndTime, now);
                case ENDED -> query.lt(Contest::getEndTime, now);
            }
        }

        query.orderByDesc(Contest::getUpdatedAt);

        Page<Contest> result = contestMapper.selectPage(pager, query);
        List<Contest> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(List.of(), result.getTotal(), result.getCurrent(), result.getSize());
        }

        List<Long> contestIds = records.stream().map(Contest::getId).toList();
        Map<Long, Integer> problemCounts = loadContestProblemCounts(contestIds);
        Map<Long, Integer> participantCounts = loadContestParticipantCounts(contestIds);
        Map<Long, LocalDateTime> lastSubmissionTimes = loadContestLastSubmissionTimes(contestIds);

        List<ContestSummaryView> items = new ArrayList<>(records.size());
        for (Contest contest : records) {
            ContestStatus calculatedStatus = inferStatus(contest, now);
            items.add(
                    new ContestSummaryView(
                            contest.getId(),
                            contest.getTitle(),
                            contest.getKind(),
                            contest.getIsVisible() != null && contest.getIsVisible() == 1,
                            calculatedStatus,
                            contest.getStartTime(),
                            contest.getEndTime(),
                            problemCounts.getOrDefault(contest.getId(), 0),
                            participantCounts.getOrDefault(contest.getId(), 0),
                            lastSubmissionTimes.get(contest.getId()),
                            contest.getUpdatedAt()));
        }
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional(readOnly = true)
    public ContestDetailView getContest(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "比赛不存在");
        }
        return buildContestDetail(contest);
    }

    public ContestDetailView createContest(@Valid ContestUpsertRequest request) {
        validateContestTime(request.startTime(), request.endTime());
        ContestKind kind = ContestKind.fromCode(request.kind());
        Contest contest = new Contest();
        contest.setTitle(request.title().trim());
        contest.setDescriptionMd(request.descriptionMd());
        contest.setKind(kind.getCode());
        contest.setStartTime(request.startTime());
        contest.setEndTime(request.endTime());
        contest.setIsVisible(Boolean.TRUE.equals(request.visible()) ? 1 : 0);
        contest.setCreatedBy(request.createdBy());
        contestMapper.insert(contest);
        log.info("创建比赛成功，ID={}", contest.getId());
        return getContest(contest.getId());
    }

    public ContestDetailView updateContest(Long contestId, @Valid ContestUpsertRequest request) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "比赛不存在");
        }
        validateContestTime(request.startTime(), request.endTime());
        ContestKind kind = ContestKind.fromCode(request.kind());

        contest.setTitle(request.title().trim());
        contest.setDescriptionMd(request.descriptionMd());
        contest.setKind(kind.getCode());
        contest.setStartTime(request.startTime());
        contest.setEndTime(request.endTime());
        contest.setIsVisible(Boolean.TRUE.equals(request.visible()) ? 1 : 0);
        if (request.createdBy() != null) {
            contest.setCreatedBy(request.createdBy());
        }
        contestMapper.updateById(contest);
        log.info("更新比赛成功，ID={}", contestId);
        return getContest(contestId);
    }

    public void deleteContest(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            return;
        }
        contestMapper.deleteById(contestId);

        LambdaQueryWrapper<ContestProblem> deleteProblems =
                Wrappers.lambdaQuery(ContestProblem.class).eq(ContestProblem::getContestId, contestId);
        contestProblemMapper.delete(deleteProblems);

        LambdaQueryWrapper<ContestParticipant> deleteParticipants =
                Wrappers.lambdaQuery(ContestParticipant.class)
                        .eq(ContestParticipant::getContestId, contestId);
        contestParticipantMapper.delete(deleteParticipants);
        log.info("删除比赛成功，ID={}", contestId);
    }

    public List<ContestProblemView> replaceContestProblems(
            Long contestId, ContestProblemsUpsertRequest request) {
        ensureContestExists(contestId);
        List<ContestProblemUpsertRequest> problems = request.problems();
        List<Long> problemIds = problems.stream().map(ContestProblemUpsertRequest::problemId).toList();
        if (hasDuplicate(problemIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目不能重复添加");
        }

        LambdaQueryWrapper<ContestProblem> deleteExisting =
                Wrappers.lambdaQuery(ContestProblem.class).eq(ContestProblem::getContestId, contestId);
        contestProblemMapper.delete(deleteExisting);

        for (ContestProblemUpsertRequest item : problems) {
            ContestProblem entity = new ContestProblem();
            entity.setContestId(contestId);
            entity.setProblemId(item.problemId());
            entity.setAlias(StringUtils.hasText(item.alias()) ? item.alias().trim() : null);
            entity.setPoints(item.points());
            entity.setOrderNo(item.orderNo() == null ? 0 : item.orderNo());
            contestProblemMapper.insert(entity);
        }
        log.info("更新比赛题目成功，contestId={}, size={}", contestId, problems.size());
        return loadContestProblems(contestId);
    }

    public void removeContestProblem(Long contestId, Long problemId) {
        ensureContestExists(contestId);
        LambdaQueryWrapper<ContestProblem> delete =
                Wrappers.lambdaQuery(ContestProblem.class)
                        .eq(ContestProblem::getContestId, contestId)
                        .eq(ContestProblem::getProblemId, problemId);
        contestProblemMapper.delete(delete);
        log.info("移除比赛题目，contestId={}, problemId={}", contestId, problemId);
    }

    public List<ContestParticipantView> addParticipants(
            Long contestId, ContestParticipantsUpsertRequest request) {
        ensureContestExists(contestId);
        List<Long> userIds = request.userIds();
        if (hasDuplicate(userIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "参赛者列表存在重复用户");
        }

        List<ContestParticipant> existing =
                contestParticipantMapper.selectList(
                        Wrappers.lambdaQuery(ContestParticipant.class)
                                .eq(ContestParticipant::getContestId, contestId)
                                .in(ContestParticipant::getUserId, userIds));
        Set<Long> existingUserIds =
                existing.stream().map(ContestParticipant::getUserId).collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        for (Long userId : userIds) {
            if (existingUserIds.contains(userId)) {
                continue;
            }
            ContestParticipant participant = new ContestParticipant();
            participant.setContestId(contestId);
            participant.setUserId(userId);
            participant.setRegisteredAt(now);
            contestParticipantMapper.insert(participant);
        }
        log.info("批量新增参赛者，contestId={}, size={}", contestId, userIds.size());
        return loadContestParticipants(contestId);
    }

    public void removeParticipant(Long contestId, Long userId) {
        ensureContestExists(contestId);
        LambdaQueryWrapper<ContestParticipant> delete =
                Wrappers.lambdaQuery(ContestParticipant.class)
                        .eq(ContestParticipant::getContestId, contestId)
                        .eq(ContestParticipant::getUserId, userId);
        contestParticipantMapper.delete(delete);
        log.info("移除参赛者，contestId={}, userId={}", contestId, userId);
    }

    @Transactional(readOnly = true)
    public ContestOptionsResponse loadOptions() {
        List<ContestKindOption> kindOptions =
                List.of(
                        new ContestKindOption(ContestKind.ICPC.getCode(), ContestKind.ICPC.getDisplayName()),
                        new ContestKindOption(ContestKind.OI.getCode(), ContestKind.OI.getDisplayName()),
                        new ContestKindOption(ContestKind.IOI.getCode(), ContestKind.IOI.getDisplayName()),
                        new ContestKindOption(ContestKind.CF.getCode(), ContestKind.CF.getDisplayName()),
                        new ContestKindOption(ContestKind.ACM.getCode(), ContestKind.ACM.getDisplayName()),
                        new ContestKindOption(ContestKind.CUSTOM.getCode(), ContestKind.CUSTOM.getDisplayName()));
        List<String> statuses =
                List.of(
                        ContestStatus.UPCOMING.name(),
                        ContestStatus.RUNNING.name(),
                        ContestStatus.ENDED.name());
        return new ContestOptionsResponse(kindOptions, statuses);
    }

    private ContestDetailView buildContestDetail(Contest contest) {
        ContestStatus status = inferStatus(contest, LocalDateTime.now());
        List<ContestProblemView> problems = loadContestProblems(contest.getId());
        List<ContestParticipantView> participants = loadContestParticipants(contest.getId());
        return new ContestDetailView(
                contest.getId(),
                contest.getTitle(),
                contest.getDescriptionMd(),
                contest.getKind(),
                contest.getIsVisible() != null && contest.getIsVisible() == 1,
                status,
                contest.getStartTime(),
                contest.getEndTime(),
                contest.getCreatedBy(),
                contest.getCreatedAt(),
                contest.getUpdatedAt(),
                problems.size(),
                participants.size(),
                problems,
                participants);
    }

    private List<ContestProblemView> loadContestProblems(Long contestId) {
        List<ContestProblem> relations =
                contestProblemMapper.selectList(
                        Wrappers.lambdaQuery(ContestProblem.class)
                                .eq(ContestProblem::getContestId, contestId)
                                .orderByAsc(ContestProblem::getOrderNo, ContestProblem::getProblemId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> problemIds = relations.stream().map(ContestProblem::getProblemId).toList();
        Map<Long, Problem> problemMap =
                problemMapper
                        .selectBatchIds(problemIds)
                        .stream()
                        .collect(Collectors.toMap(Problem::getId, problem -> problem));

        Map<Long, ProblemStatement> statementMap = loadProblemStatements(problemIds);
        Map<Long, ProblemStatsView> statsMap = loadProblemStats(problemIds);

        List<ContestProblemView> views = new ArrayList<>(relations.size());
        for (ContestProblem relation : relations) {
            Problem problem = problemMap.get(relation.getProblemId());
            ProblemStatement statement = statementMap.get(relation.getProblemId());
            ProblemStatsView stats = statsMap.get(relation.getProblemId());
            views.add(
                    new ContestProblemView(
                            relation.getContestId(),
                            relation.getProblemId(),
                            problem != null ? problem.getSlug() : null,
                            statement != null ? statement.getTitle() : null,
                            relation.getAlias(),
                            relation.getPoints(),
                            relation.getOrderNo(),
                            stats != null ? stats.getLastSubmissionAt() : null,
                            stats != null ? stats.getSubmissionCount() : null,
                            stats != null ? stats.getSolvedCount() : null,
                            stats != null ? stats.getAcceptanceRate() : null));
        }
        views.sort(Comparator.comparing(ContestProblemView::orderNo, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(ContestProblemView::problemId));
        return views;
    }

    private Map<Long, ProblemStatement> loadProblemStatements(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        List<ProblemStatement> statements =
                problemStatementMapper.selectList(
                        Wrappers.lambdaQuery(ProblemStatement.class)
                                .in(ProblemStatement::getProblemId, problemIds)
                                .eq(ProblemStatement::getLangCode, DEFAULT_STATEMENT_LANG));
        return statements.stream()
                .collect(Collectors.toMap(ProblemStatement::getProblemId, statement -> statement, (left, right) -> left));
    }

    private Map<Long, ProblemStatsView> loadProblemStats(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        List<ProblemStatsView> stats =
                problemStatsViewMapper.selectList(
                        Wrappers.lambdaQuery(ProblemStatsView.class)
                                .in(ProblemStatsView::getProblemId, problemIds));
        return stats.stream()
                .collect(Collectors.toMap(ProblemStatsView::getProblemId, view -> view, (left, right) -> left));
    }

    private List<ContestParticipantView> loadContestParticipants(Long contestId) {
        List<ContestParticipant> relations =
                contestParticipantMapper.selectList(
                        Wrappers.lambdaQuery(ContestParticipant.class)
                                .eq(ContestParticipant::getContestId, contestId)
                                .orderByAsc(ContestParticipant::getRegisteredAt));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = relations.stream().map(ContestParticipant::getUserId).toList();
        Map<Long, User> userMap =
                userMapper
                        .selectBatchIds(userIds)
                        .stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        List<ContestParticipantView> views = new ArrayList<>(relations.size());
        for (ContestParticipant relation : relations) {
            User user = userMap.get(relation.getUserId());
            views.add(
                    new ContestParticipantView(
                            relation.getContestId(),
                            relation.getUserId(),
                            user != null ? user.getUsername() : null,
                            user != null ? user.getBio() : null,
                            relation.getRegisteredAt()));
        }
        return views;
    }

    private void ensureContestExists(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "比赛不存在");
        }
    }

    private void validateContestTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "比赛时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "比赛结束时间必须晚于开始时间");
        }
    }

    private ContestStatus inferStatus(Contest contest, LocalDateTime reference) {
        if (contest.getStartTime() != null && contest.getStartTime().isAfter(reference)) {
            return ContestStatus.UPCOMING;
        }
        if (contest.getEndTime() != null && contest.getEndTime().isBefore(reference)) {
            return ContestStatus.ENDED;
        }
        return ContestStatus.RUNNING;
    }

    private Map<Long, Integer> loadContestProblemCounts(List<Long> contestIds) {
        if (contestIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<ContestProblem> query =
                Wrappers.query();
        query.select("contest_id", "COUNT(1) AS cnt");
        query.in("contest_id", contestIds);
        query.groupBy("contest_id");
        List<Map<String, Object>> rows = contestProblemMapper.selectMaps(query);
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long contestId = ((Number) row.get("contest_id")).longValue();
            Integer count = ((Number) row.get("cnt")).intValue();
            result.put(contestId, count);
        }
        return result;
    }

    private Map<Long, Integer> loadContestParticipantCounts(List<Long> contestIds) {
        if (contestIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<ContestParticipant> query = Wrappers.query();
        query.select("contest_id", "COUNT(1) AS cnt");
        query.in("contest_id", contestIds);
        query.groupBy("contest_id");
        List<Map<String, Object>> rows = contestParticipantMapper.selectMaps(query);
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long contestId = ((Number) row.get("contest_id")).longValue();
            Integer count = ((Number) row.get("cnt")).intValue();
            result.put(contestId, count);
        }
        return result;
    }

    private Map<Long, LocalDateTime> loadContestLastSubmissionTimes(List<Long> contestIds) {
        if (contestIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<com.david.contest.entity.Submission> query = Wrappers.query();
        query.select("contest_id", "MAX(created_at) AS latest");
        query.in("contest_id", contestIds);
        query.groupBy("contest_id");
        List<Map<String, Object>> rows = submissionMapper.selectMaps(query);
        Map<Long, LocalDateTime> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object contestIdObj = row.get("contest_id");
            Object latestObj = row.get("latest");
            if (contestIdObj == null || latestObj == null) {
                continue;
            }
            Long contestId = ((Number) contestIdObj).longValue();
            LocalDateTime latest;
            if (latestObj instanceof LocalDateTime time) {
                latest = time;
            } else if (latestObj instanceof java.sql.Timestamp ts) {
                latest = ts.toLocalDateTime();
            } else {
                latest = LocalDateTime.parse(latestObj.toString().replace(" ", "T"));
            }
            result.put(contestId, latest);
        }
        return result;
    }

    private boolean hasDuplicate(List<Long> values) {
        return values.size() != values.stream().collect(Collectors.toSet()).size();
    }
}
