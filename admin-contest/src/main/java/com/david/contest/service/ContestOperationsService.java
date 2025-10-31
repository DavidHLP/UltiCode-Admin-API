package com.david.contest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.contest.dto.ContestDetailView;
import com.david.contest.dto.ContestParticipantView;
import com.david.contest.dto.ContestProblemView;
import com.david.contest.dto.ContestScoreboardParticipantView;
import com.david.contest.dto.ContestScoreboardProblemView;
import com.david.contest.dto.ContestScoreboardRecordView;
import com.david.contest.dto.ContestScoreboardView;
import com.david.contest.dto.ContestSubmissionView;
import com.david.contest.dto.PageResult;
import com.david.contest.entity.Contest;
import com.david.contest.entity.ContestProblem;
import com.david.contest.entity.Submission;
import com.david.contest.entity.UserProblemBestView;
import com.david.contest.enums.ContestKind;
import com.david.contest.mapper.ContestMapper;
import com.david.contest.mapper.ContestProblemMapper;
import com.david.contest.mapper.SubmissionMapper;
import com.david.contest.mapper.UserMapper;
import com.david.contest.mapper.UserProblemBestViewMapper;
import com.david.core.exception.BusinessException;

import lombok.Getter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ContestOperationsService {

    private static final String VERDICT_ACCEPTED = "AC";

    private final ContestMapper contestMapper;
    private final ContestProblemMapper contestProblemMapper;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;
    private final UserProblemBestViewMapper userProblemBestViewMapper;
    private final ContestPlanningService contestPlanningService;

    public ContestOperationsService(
            ContestMapper contestMapper,
            ContestProblemMapper contestProblemMapper,
            SubmissionMapper submissionMapper,
            UserMapper userMapper,
            UserProblemBestViewMapper userProblemBestViewMapper,
            ContestPlanningService contestPlanningService) {
        this.contestMapper = contestMapper;
        this.contestProblemMapper = contestProblemMapper;
        this.submissionMapper = submissionMapper;
        this.userMapper = userMapper;
        this.userProblemBestViewMapper = userProblemBestViewMapper;
        this.contestPlanningService = contestPlanningService;
    }

    private static String key(Long userId, Long problemId) {
        return userId + ":" + problemId;
    }

    public ContestScoreboardView generateScoreboard(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "比赛不存在");
        }
        LocalDateTime generatedAt = LocalDateTime.now();
        int penaltyPerWrong =
                contest.getPenaltyPerWrong() != null && contest.getPenaltyPerWrong() > 0
                        ? contest.getPenaltyPerWrong()
                        : 20;
        int freezeMinutes =
                contest.getScoreboardFreezeMinutes() != null
                                && contest.getScoreboardFreezeMinutes() > 0
                        ? contest.getScoreboardFreezeMinutes()
                        : 0;
        boolean freezeHideScore =
                contest.getHideScoreDuringFreeze() != null
                        && contest.getHideScoreDuringFreeze() == 1;
        LocalDateTime freezeStartTime = null;
        boolean freezeWindowActive = false;
        if (freezeMinutes > 0 && contest.getEndTime() != null) {
            freezeStartTime = contest.getEndTime().minusMinutes(freezeMinutes);
            if (!generatedAt.isBefore(freezeStartTime)
                    && generatedAt.isBefore(contest.getEndTime())) {
                freezeWindowActive = true;
            }
        }

        ContestDetailView detail = contestPlanningService.getContest(contestId);
        List<ContestProblemView> problemViews = detail.problems();
        List<ContestParticipantView> participantViews = detail.participants();

        boolean freezeActive = freezeWindowActive && freezeHideScore;
        int pendingSubmissionTotal = 0;

        Map<Long, ProblemContext> problemContexts = buildProblemContexts(problemViews);
        Map<Long, ParticipantContext> participantContexts =
                buildParticipantContexts(participantViews);

        List<Long> participantIds = participantContexts.keySet().stream().toList();
        if (participantIds.isEmpty() || problemContexts.isEmpty()) {
            return new ContestScoreboardView(
                    contestId,
                    contest.getKind(),
                    generatedAt,
                    penaltyPerWrong,
                    freezeWindowActive && freezeHideScore,
                    freezeHideScore,
                    freezeStartTime,
                    freezeMinutes,
                    0,
                    buildScoreboardProblemViews(problemContexts),
                    List.of());
        }

        Map<Long, Set<Long>> participantProblemIndex =
                participantContexts.values().stream()
                        .collect(
                                Collectors.toMap(
                                        ParticipantContext::userId,
                                        ctx -> new LinkedHashSet<>(problemContexts.keySet())));

        List<Submission> submissions =
                submissionMapper.selectList(
                        Wrappers.lambdaQuery(Submission.class)
                                .eq(Submission::getContestId, contestId)
                                .in(Submission::getUserId, participantIds)
                                .orderByAsc(Submission::getCreatedAt, Submission::getId));

        Map<String, Integer> globalBestScoreMap =
                loadGlobalBestScoreMap(participantIds, problemContexts.keySet());

        ScoreboardStrategy strategy = resolveStrategy(contest.getKind());
        for (Submission submission : submissions) {
            ParticipantContext participant = participantContexts.get(submission.getUserId());
            if (participant == null) {
                continue;
            }
            ProblemContext problem = problemContexts.get(submission.getProblemId());
            if (problem == null) {
                continue;
            }
            ParticipantProblemState state =
                    participant.problemStates.computeIfAbsent(
                            submission.getProblemId(),
                            id -> new ParticipantProblemState(problem.alias()));
            LocalDateTime submissionTime = submission.getCreatedAt();
            if (submissionTime != null
                    && (participant.lastSubmissionAt == null
                            || submissionTime.isAfter(participant.lastSubmissionAt))) {
                participant.lastSubmissionAt = submissionTime;
            }

            boolean isPending =
                    freezeWindowActive
                            && freezeStartTime != null
                            && submissionTime != null
                            && submissionTime.isAfter(freezeStartTime);
            if (isPending) {
                state.pendingAttempts += 1;
                state.pendingLastVerdict = submission.getVerdict();
                state.pendingLastSubmissionAt = submissionTime;
                if (submission.getScore() != null) {
                    int score = submission.getScore();
                    if (state.pendingBestScore == null || score > state.pendingBestScore) {
                        state.pendingBestScore = score;
                    }
                }
                participant.pendingSubmissionCount += 1;
                pendingSubmissionTotal += 1;
                continue;
            }

            state.totalAttempts += 1;
            state.lastSubmissionAt = submissionTime;
            state.lastVerdict = submission.getVerdict();
            if (submission.getScore() != null) {
                int score = submission.getScore();
                if (state.bestScore == null || score > state.bestScore) {
                    state.bestScore = score;
                }
            }
            if (VERDICT_ACCEPTED.equalsIgnoreCase(submission.getVerdict())) {
                if (state.firstAcceptedAt == null) {
                    state.firstAcceptedAt = submission.getCreatedAt();
                    state.wrongAttemptsBeforeAc = state.totalAttempts - 1;
                }
            }
        }

        List<ContestScoreboardParticipantView> participants =
                buildParticipantScoreboardViews(
                        contest,
                        problemContexts,
                        participantContexts,
                        participantProblemIndex,
                        strategy,
                        globalBestScoreMap,
                        penaltyPerWrong);

        return new ContestScoreboardView(
                contestId,
                contest.getKind(),
                generatedAt,
                penaltyPerWrong,
                freezeActive,
                freezeHideScore,
                freezeStartTime,
                freezeMinutes,
                pendingSubmissionTotal,
                buildScoreboardProblemViews(problemContexts),
                participants);
    }

    public PageResult<ContestSubmissionView> listSubmissions(
            Long contestId, int page, int size, String verdict, Long userId, Long problemId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "比赛不存在");
        }
        Page<Submission> pager = new Page<>(page, size);
        LambdaQueryWrapper<Submission> query = Wrappers.lambdaQuery(Submission.class);
        query.eq(Submission::getContestId, contestId);
        if (userId != null) {
            query.eq(Submission::getUserId, userId);
        }
        if (problemId != null) {
            query.eq(Submission::getProblemId, problemId);
        }
        if (verdict != null && !verdict.isBlank()) {
            query.eq(Submission::getVerdict, verdict.trim().toUpperCase(Locale.ROOT));
        }
        query.orderByDesc(Submission::getCreatedAt).orderByDesc(Submission::getId);

        Page<Submission> result = submissionMapper.selectPage(pager, query);
        if (CollectionUtils.isEmpty(result.getRecords())) {
            return new PageResult<>(
                    List.of(), result.getTotal(), result.getCurrent(), result.getSize());
        }
        Map<Long, String> userNameMap =
                userMapper
                        .selectByIds(
                                result.getRecords().stream()
                                        .map(Submission::getUserId)
                                        .collect(Collectors.toSet()))
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        com.david.contest.entity.User::getId,
                                        com.david.contest.entity.User::getUsername));

        Map<Long, ContestProblem> problemMap =
                contestProblemMapper
                        .selectList(
                                Wrappers.lambdaQuery(ContestProblem.class)
                                        .eq(ContestProblem::getContestId, contestId))
                        .stream()
                        .collect(
                                Collectors.toMap(ContestProblem::getProblemId, problem -> problem));

        List<ContestSubmissionView> views =
                result.getRecords().stream()
                        .map(
                                submission -> {
                                    ContestProblem relation =
                                            problemMap.get(submission.getProblemId());
                                    return new ContestSubmissionView(
                                            submission.getId(),
                                            submission.getUserId(),
                                            userNameMap.get(submission.getUserId()),
                                            submission.getProblemId(),
                                            relation != null ? relation.getAlias() : null,
                                            submission.getVerdict(),
                                            submission.getScore(),
                                            submission.getTimeMs(),
                                            submission.getMemoryKb(),
                                            submission.getCreatedAt());
                                })
                        .toList();
        return new PageResult<>(views, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private Map<String, Integer> loadGlobalBestScoreMap(
            List<Long> participantIds, Set<Long> problemIds) {
        if (participantIds.isEmpty() || problemIds.isEmpty()) {
            return Map.of();
        }
        List<UserProblemBestView> bestList =
                userProblemBestViewMapper.selectList(
                        Wrappers.lambdaQuery(UserProblemBestView.class)
                                .in(UserProblemBestView::getUserId, participantIds)
                                .in(UserProblemBestView::getProblemId, problemIds));
        Map<String, Integer> map = new LinkedHashMap<>();
        for (UserProblemBestView view : bestList) {
            String key = key(view.getUserId(), view.getProblemId());
            map.put(key, view.getBestScore());
        }
        return map;
    }

    private List<ContestScoreboardParticipantView> buildParticipantScoreboardViews(
            Contest contest,
            Map<Long, ProblemContext> problemContexts,
            Map<Long, ParticipantContext> participantContexts,
            Map<Long, Set<Long>> participantProblemIndex,
            ScoreboardStrategy strategy,
            Map<String, Integer> globalBestScoreMap,
            int penaltyPerWrong) {

        for (ParticipantContext participant : participantContexts.values()) {
            Set<Long> problems = participantProblemIndex.get(participant.userId());
            if (problems == null) {
                continue;
            }
            for (Long problemId : problems) {
                participant.problemStates.computeIfAbsent(
                        problemId,
                        id -> new ParticipantProblemState(problemContexts.get(id).alias()));
            }
            computeParticipantScore(
                    contest, participant, problemContexts, strategy, penaltyPerWrong);
        }

        List<ParticipantContext> sortedParticipants =
                participantContexts.values().stream()
                        .sorted(strategy.participantComparator())
                        .toList();

        List<ContestScoreboardParticipantView> result = new ArrayList<>(sortedParticipants.size());
        ParticipantContext previous = null;
        int displayRank = 0;
        for (int index = 0; index < sortedParticipants.size(); index++) {
            ParticipantContext current = sortedParticipants.get(index);
            if (!strategy.equalsParticipants(previous, current)) {
                displayRank = index + 1;
            }
            previous = current;

            List<ContestScoreboardRecordView> recordViews =
                    problemContexts.values().stream()
                            .sorted(
                                    Comparator.comparing(
                                            ProblemContext::orderNo,
                                            Comparator.nullsFirst(Integer::compareTo)))
                            .map(
                                    ctx -> {
                                        ParticipantProblemState state =
                                                current.problemStates.getOrDefault(
                                                        ctx.problemId(),
                                                        new ParticipantProblemState(ctx.alias()));
                                        Integer globalBest =
                                                globalBestScoreMap.getOrDefault(
                                                        key(current.userId(), ctx.problemId()),
                                                        null);
                                        Integer contestPoints =
                                                ctx.points() != null ? ctx.points() : null;
                                        Integer bestScore = state.bestScore;
                                        return new ContestScoreboardRecordView(
                                                ctx.problemId(),
                                                ctx.alias(),
                                                state.totalAttempts,
                                                state.wrongAttemptsBeforeAc,
                                                bestScore,
                                                contestPoints,
                                                state.lastVerdict,
                                                state.firstAcceptedAt,
                                                state.lastSubmissionAt,
                                                globalBest,
                                                state.pendingAttempts,
                                                state.pendingBestScore,
                                                state.pendingLastVerdict,
                                                state.pendingLastSubmissionAt);
                                    })
                            .toList();

            result.add(
                    new ContestScoreboardParticipantView(
                            current.userId(),
                            current.username(),
                            current.displayName(),
                            displayRank,
                            current.solvedCount,
                            current.totalScore,
                            current.penalty,
                            current.lastAcceptedAt,
                            current.lastSubmissionAt,
                            current.pendingSubmissionCount,
                            recordViews));
        }
        return result;
    }

    private List<ContestScoreboardProblemView> buildScoreboardProblemViews(
            Map<Long, ProblemContext> problemContexts) {
        return problemContexts.values().stream()
                .sorted(
                        Comparator.comparing(
                                ProblemContext::orderNo, Comparator.nullsFirst(Integer::compareTo)))
                .map(
                        ctx ->
                                new ContestScoreboardProblemView(
                                        ctx.problemId(),
                                        ctx.alias(),
                                        ctx.title(),
                                        ctx.orderNo(),
                                        ctx.points(),
                                        ctx.submissionCount(),
                                        ctx.solvedCount(),
                                        ctx.acceptanceRate(),
                                        ctx.lastSubmissionAt()))
                .toList();
    }

    private void computeParticipantScore(
            Contest contest,
            ParticipantContext participant,
            Map<Long, ProblemContext> problemContexts,
            ScoreboardStrategy strategy,
            int penaltyPerWrong) {
        switch (strategy) {
            case ICPC -> computeIcpcScore(contest, participant, problemContexts, penaltyPerWrong);
            case OI -> computeOiScore(participant, problemContexts);
        }
    }

    private void computeIcpcScore(
            Contest contest,
            ParticipantContext participant,
            Map<Long, ProblemContext> problemContexts,
            int penaltyPerWrong) {
        int solved = 0;
        long penalty = 0;
        LocalDateTime lastAccepted = null;
        LocalDateTime lastSubmission = null;
        for (Map.Entry<Long, ParticipantProblemState> entry :
                participant.problemStates.entrySet()) {
            ParticipantProblemState state = entry.getValue();
            if (state.lastSubmissionAt != null
                    && (lastSubmission == null || state.lastSubmissionAt.isAfter(lastSubmission))) {
                lastSubmission = state.lastSubmissionAt;
            }
            if (state.firstAcceptedAt == null) {
                continue;
            }
            solved += 1;
            if (lastAccepted == null || state.firstAcceptedAt.isAfter(lastAccepted)) {
                lastAccepted = state.firstAcceptedAt;
            }
            long minutes =
                    contest.getStartTime() == null
                            ? 0
                            : Duration.between(contest.getStartTime(), state.firstAcceptedAt)
                                    .toMinutes();
            penalty += minutes + (long) state.wrongAttemptsBeforeAc * Math.max(penaltyPerWrong, 0);
        }
        participant.solvedCount = solved;
        participant.totalScore = solved;
        participant.penalty = penalty;
        participant.lastAcceptedAt = lastAccepted;
        if (lastSubmission != null
                && (participant.lastSubmissionAt == null
                        || lastSubmission.isAfter(participant.lastSubmissionAt))) {
            participant.lastSubmissionAt = lastSubmission;
        }
    }

    private void computeOiScore(
            ParticipantContext participant, Map<Long, ProblemContext> problemContexts) {
        int totalScore = 0;
        int solved = 0;
        LocalDateTime lastSubmission = null;
        for (Map.Entry<Long, ParticipantProblemState> entry :
                participant.problemStates.entrySet()) {
            ProblemContext problem = problemContexts.get(entry.getKey());
            ParticipantProblemState state = entry.getValue();
            if (state.bestScore != null) {
                int cap = problem.points() != null ? problem.points() : state.bestScore;
                totalScore += Math.min(state.bestScore, cap);
                if (state.bestScore > 0) {
                    solved += 1;
                }
            }
            if (state.lastSubmissionAt != null
                    && (lastSubmission == null || state.lastSubmissionAt.isAfter(lastSubmission))) {
                lastSubmission = state.lastSubmissionAt;
            }
        }
        participant.totalScore = totalScore;
        participant.solvedCount = solved;
        participant.penalty = 0;
        if (lastSubmission != null
                && (participant.lastSubmissionAt == null
                        || lastSubmission.isAfter(participant.lastSubmissionAt))) {
            participant.lastSubmissionAt = lastSubmission;
        }
    }

    private ScoreboardStrategy resolveStrategy(String kind) {
        ContestKind contestKind;
        try {
            contestKind = ContestKind.fromCode(kind);
        } catch (IllegalArgumentException ex) {
            return ScoreboardStrategy.OI;
        }
        return switch (contestKind) {
            case ICPC, CF, ACM -> ScoreboardStrategy.ICPC;
            case OI, IOI, CUSTOM -> ScoreboardStrategy.OI;
        };
    }

    private Map<Long, ProblemContext> buildProblemContexts(List<ContestProblemView> problemViews) {
        Map<Long, ProblemContext> result = new LinkedHashMap<>();
        for (ContestProblemView view : problemViews) {
            result.put(
                    view.problemId(),
                    new ProblemContext(
                            view.problemId(),
                            view.alias(),
                            view.problemTitle(),
                            view.orderNo(),
                            view.points(),
                            view.submissionCount(),
                            view.solvedCount(),
                            view.lastSubmissionAt(),
                            view.acceptanceRate()));
        }
        return result;
    }

    private Map<Long, ParticipantContext> buildParticipantContexts(
            List<ContestParticipantView> participantViews) {
        Map<Long, ParticipantContext> result = new LinkedHashMap<>();
        for (ContestParticipantView view : participantViews) {
            result.put(
                    view.userId(),
                    new ParticipantContext(
                            view.userId(),
                            view.username(),
                            view.displayName(),
                            view.registeredAt()));
        }
        return result;
    }

    private enum ScoreboardStrategy {
        ICPC,
        OI;

        public Comparator<ParticipantContext> participantComparator() {
            return switch (this) {
                case ICPC ->
                        Comparator.<ParticipantContext>comparingInt(ctx -> -ctx.solvedCount)
                                .thenComparingLong(ctx -> ctx.penalty)
                                .thenComparing(ctx -> ctx.lastAcceptedAt, nullableComparator())
                                .thenComparing(ctx -> ctx.lastSubmissionAt, nullableComparator())
                                .thenComparing(
                                        ParticipantContext::username,
                                        Comparator.nullsLast(String::compareToIgnoreCase));
                case OI ->
                        Comparator.<ParticipantContext>comparingInt(ctx -> -ctx.totalScore)
                                .thenComparingInt(ctx -> -ctx.solvedCount)
                                .thenComparing(ctx -> ctx.lastSubmissionAt, nullableComparator())
                                .thenComparing(
                                        ParticipantContext::username,
                                        Comparator.nullsLast(String::compareToIgnoreCase));
            };
        }

        private Comparator<LocalDateTime> nullableComparator() {
            return (left, right) -> {
                if (left == null && right == null) {
                    return 0;
                }
                if (left == null) {
                    return 1;
                }
                if (right == null) {
                    return -1;
                }
                return left.compareTo(right);
            };
        }

        public boolean equalsParticipants(ParticipantContext left, ParticipantContext right) {
            if (left == null || right == null) {
                return false;
            }
            return switch (this) {
                case ICPC -> left.solvedCount == right.solvedCount && left.penalty == right.penalty;
                case OI ->
                        left.totalScore == right.totalScore
                                && left.solvedCount == right.solvedCount;
            };
        }
    }

    private record ProblemContext(
            Long problemId,
            String alias,
            String title,
            Integer orderNo,
            Integer points,
            Integer submissionCount,
            Integer solvedCount,
            LocalDateTime lastSubmissionAt,
            BigDecimal acceptanceRate) {}

    private static final class ParticipantContext {
        private final Long userId;
        private final String username;
        private final String displayName;
        @Getter private final LocalDateTime registeredAt;
        private final Map<Long, ParticipantProblemState> problemStates = new LinkedHashMap<>();
        private int solvedCount;
        private int totalScore;
        private long penalty;
        private LocalDateTime lastAcceptedAt;
        private LocalDateTime lastSubmissionAt;
        private int pendingSubmissionCount;

        private ParticipantContext(
                Long userId, String username, String displayName, LocalDateTime registeredAt) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.registeredAt = registeredAt;
        }

        public Long userId() {
            return userId;
        }

        public String username() {
            return username;
        }

        public String displayName() {
            return displayName;
        }
    }

    private static final class ParticipantProblemState {
        @Getter private final String alias;
        private int totalAttempts;
        private int wrongAttemptsBeforeAc;
        private Integer bestScore;
        private String lastVerdict;
        private LocalDateTime firstAcceptedAt;
        private LocalDateTime lastSubmissionAt;
        private int pendingAttempts;
        private Integer pendingBestScore;
        private String pendingLastVerdict;
        private LocalDateTime pendingLastSubmissionAt;

        private ParticipantProblemState(String alias) {
            this.alias = alias;
        }
    }
}
