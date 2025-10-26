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
import com.david.contest.dto.ContestRegistrationCreateRequest;
import com.david.contest.dto.ContestRegistrationDecisionRequest;
import com.david.contest.dto.ContestRegistrationView;
import com.david.contest.dto.ContestSummaryView;
import com.david.contest.dto.ContestUpsertRequest;
import com.david.contest.dto.PageResult;
import com.david.contest.entity.Contest;
import com.david.contest.entity.ContestParticipant;
import com.david.contest.entity.ContestProblem;
import com.david.contest.entity.ContestRegistration;
import com.david.contest.entity.Problem;
import com.david.contest.entity.ProblemStatement;
import com.david.contest.entity.ProblemStatsView;
import com.david.contest.entity.User;
import com.david.contest.enums.ContestKind;
import com.david.contest.enums.ContestRegistrationMode;
import com.david.contest.enums.ContestRegistrationSource;
import com.david.contest.enums.ContestRegistrationStatus;
import com.david.contest.enums.ContestStatus;
import com.david.core.exception.BusinessException;
import com.david.contest.mapper.ContestMapper;
import com.david.contest.mapper.ContestParticipantMapper;
import com.david.contest.mapper.ContestProblemMapper;
import com.david.contest.mapper.ContestRegistrationMapper;
import com.david.contest.mapper.ProblemMapper;
import com.david.contest.mapper.ProblemStatementMapper;
import com.david.contest.mapper.ProblemStatsViewMapper;
import com.david.contest.mapper.SubmissionMapper;
import com.david.contest.mapper.UserMapper;
import com.david.contest.dto.ProblemSummaryOption;
import com.david.contest.dto.UserSummaryOption;
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
import java.util.EnumSet;
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
    private final ContestRegistrationMapper contestRegistrationMapper;
    private final SubmissionMapper submissionMapper;
    private final ProblemMapper problemMapper;
    private final ProblemStatementMapper problemStatementMapper;
    private final ProblemStatsViewMapper problemStatsViewMapper;
    private final UserMapper userMapper;

    public ContestPlanningService(
            ContestMapper contestMapper,
            ContestProblemMapper contestProblemMapper,
            ContestParticipantMapper contestParticipantMapper,
            ContestRegistrationMapper contestRegistrationMapper,
            SubmissionMapper submissionMapper,
            ProblemMapper problemMapper,
            ProblemStatementMapper problemStatementMapper,
            ProblemStatsViewMapper problemStatsViewMapper,
            UserMapper userMapper) {
        this.contestMapper = contestMapper;
        this.contestProblemMapper = contestProblemMapper;
        this.contestParticipantMapper = contestParticipantMapper;
        this.contestRegistrationMapper = contestRegistrationMapper;
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
            ContestStatus desired = ContestStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
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
                            contest.getRegistrationMode(),
                            contest.getRegistrationStartTime(),
                            contest.getRegistrationEndTime(),
                            contest.getMaxParticipants(),
                            contest.getPenaltyPerWrong(),
                            contest.getScoreboardFreezeMinutes(),
                            contest.getHideScoreDuringFreeze() != null
                                    && contest.getHideScoreDuringFreeze() == 1,
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
        applyContestConfig(contest, request);
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
        applyContestConfig(contest, request);
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

        LambdaQueryWrapper<ContestProblem> deleteProblems = Wrappers.lambdaQuery(ContestProblem.class)
                .eq(ContestProblem::getContestId, contestId);
        contestProblemMapper.delete(deleteProblems);

        LambdaQueryWrapper<ContestParticipant> deleteParticipants = Wrappers.lambdaQuery(ContestParticipant.class)
                .eq(ContestParticipant::getContestId, contestId);
        contestParticipantMapper.delete(deleteParticipants);
        log.info("删除比赛成功，ID={}", contestId);
    }

    public List<ContestProblemView> replaceContestProblems(
            Long contestId, ContestProblemsUpsertRequest request) {
        requireContest(contestId);
        List<ContestProblemUpsertRequest> problems = request.problems();
        List<Long> problemIds = problems.stream().map(ContestProblemUpsertRequest::problemId).toList();
        if (hasDuplicate(problemIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目不能重复添加");
        }

        LambdaQueryWrapper<ContestProblem> deleteExisting = Wrappers.lambdaQuery(ContestProblem.class)
                .eq(ContestProblem::getContestId, contestId);
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
        requireContest(contestId);
        LambdaQueryWrapper<ContestProblem> delete = Wrappers.lambdaQuery(ContestProblem.class)
                .eq(ContestProblem::getContestId, contestId)
                .eq(ContestProblem::getProblemId, problemId);
        contestProblemMapper.delete(delete);
        log.info("移除比赛题目，contestId={}, problemId={}", contestId, problemId);
    }

    public List<ContestParticipantView> addParticipants(
            Long contestId, ContestParticipantsUpsertRequest request, Long operatorId) {
        Contest contest = requireContest(contestId);
        List<Long> userIds = request.userIds();
        if (hasDuplicate(userIds)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "参赛者列表存在重复用户");
        }

        List<ContestParticipant> existing = contestParticipantMapper.selectList(
                Wrappers.lambdaQuery(ContestParticipant.class)
                        .eq(ContestParticipant::getContestId, contestId)
                        .in(ContestParticipant::getUserId, userIds));
        Set<Long> existingUserIds = existing.stream().map(ContestParticipant::getUserId).collect(Collectors.toSet());

        int incoming = (int) userIds.stream().filter(id -> !existingUserIds.contains(id)).count();
        assertParticipantCapacity(contest, incoming);

        Map<Long, ContestRegistration> registrationMap = contestRegistrationMapper
                .selectList(
                        Wrappers.lambdaQuery(ContestRegistration.class)
                                .eq(ContestRegistration::getContestId, contestId)
                                .in(ContestRegistration::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(ContestRegistration::getUserId, reg -> reg));

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

            upsertApprovedRegistration(
                    contestId, userId, operatorId, registrationMap.get(userId), "后台加赛", false);
        }
        log.info("批量新增参赛者，contestId={}, size={}", contestId, userIds.size());
        return loadContestParticipants(contestId);
    }

    public void removeParticipant(Long contestId, Long userId, Long operatorId) {
        requireContest(contestId);
        LambdaQueryWrapper<ContestParticipant> delete = Wrappers.lambdaQuery(ContestParticipant.class)
                .eq(ContestParticipant::getContestId, contestId)
                .eq(ContestParticipant::getUserId, userId);
        contestParticipantMapper.delete(delete);
        ContestRegistration registration = contestRegistrationMapper.selectOne(
                Wrappers.lambdaQuery(ContestRegistration.class)
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, userId));
        if (registration != null
                && !ContestRegistrationStatus.CANCELLED
                        .getCode()
                        .equalsIgnoreCase(registration.getStatus())) {
            registration.setStatus(ContestRegistrationStatus.CANCELLED.getCode());
            registration.setReviewedBy(operatorId);
            registration.setReviewedAt(LocalDateTime.now());
            registration.setNote("管理员移除参赛资格");
            contestRegistrationMapper.updateById(registration);
        }
        log.info("移除参赛者，contestId={}, userId={}", contestId, userId);
    }

    @Transactional(readOnly = true)
    public PageResult<ContestRegistrationView> listRegistrations(
            Long contestId, ContestRegistrationStatus status, int page, int size) {
        requireContest(contestId);
        Page<ContestRegistration> pager = new Page<>(page, size);
        LambdaQueryWrapper<ContestRegistration> query = Wrappers.lambdaQuery(ContestRegistration.class)
                .eq(ContestRegistration::getContestId, contestId)
                .orderByDesc(ContestRegistration::getCreatedAt)
                .orderByDesc(ContestRegistration::getId);
        if (status != null) {
            query.eq(ContestRegistration::getStatus, status.getCode());
        }
        Page<ContestRegistration> result = contestRegistrationMapper.selectPage(pager, query);
        if (CollectionUtils.isEmpty(result.getRecords())) {
            return new PageResult<>(List.of(), result.getTotal(), result.getCurrent(), result.getSize());
        }
        Set<Long> userIds = result.getRecords().stream().map(ContestRegistration::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<Long> reviewerIds = result.getRecords().stream()
                .map(ContestRegistration::getReviewedBy)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, User> reviewerMap = reviewerIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(reviewerIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        List<ContestRegistrationView> items = result.getRecords().stream()
                .map(
                        registration -> {
                            User user = userMap.get(registration.getUserId());
                            User reviewer = reviewerMap.get(registration.getReviewedBy());
                            return new ContestRegistrationView(
                                    registration.getId(),
                                    registration.getContestId(),
                                    registration.getUserId(),
                                    user != null ? user.getUsername() : null,
                                    user != null ? user.getBio() : null,
                                    ContestRegistrationStatus.fromCode(registration.getStatus()),
                                    registration.getSource(),
                                    registration.getNote(),
                                    registration.getReviewedBy(),
                                    reviewer != null ? reviewer.getUsername() : null,
                                    registration.getReviewedAt(),
                                    registration.getCreatedAt());
                        })
                .toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public ContestRegistrationView createRegistration(
            Long contestId, ContestRegistrationCreateRequest request, Long operatorId) {
        Contest contest = requireContest(contestId);
        LocalDateTime now = LocalDateTime.now();
        if (contest.getRegistrationStartTime() != null
                && now.isBefore(contest.getRegistrationStartTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报名尚未开始");
        }
        if (contest.getRegistrationEndTime() != null
                && now.isAfter(contest.getRegistrationEndTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "报名已结束");
        }
        ContestRegistrationSource source = request.source() == null ? ContestRegistrationSource.SELF : request.source();
        ContestRegistrationMode mode = ContestRegistrationMode.fromCode(contest.getRegistrationMode());
        if (mode == ContestRegistrationMode.INVITE_ONLY
                && source == ContestRegistrationSource.SELF) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "当前仅支持邀请报名");
        }

        ContestRegistration existing = contestRegistrationMapper.selectOne(
                Wrappers.lambdaQuery(ContestRegistration.class)
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, request.userId()));

        ContestRegistrationStatus targetStatus = switch (mode) {
            case OPEN -> ContestRegistrationStatus.APPROVED;
            case APPROVAL ->
                source == ContestRegistrationSource.ADMIN
                        ? ContestRegistrationStatus.APPROVED
                        : ContestRegistrationStatus.PENDING;
            case INVITE_ONLY ->
                source == ContestRegistrationSource.ADMIN
                        ? ContestRegistrationStatus.APPROVED
                        : ContestRegistrationStatus.PENDING;
        };

        if (existing == null) {
            ContestRegistration toCreate = new ContestRegistration();
            toCreate.setContestId(contestId);
            toCreate.setUserId(request.userId());
            toCreate.setSource(source.getCode());
            toCreate.setNote(request.note());
            toCreate.setStatus(targetStatus.getCode());
            if (targetStatus == ContestRegistrationStatus.APPROVED) {
                assertParticipantCapacity(contest, 1);
                toCreate.setReviewedBy(operatorId);
                toCreate.setReviewedAt(now);
            }
            contestRegistrationMapper.insert(toCreate);
            if (targetStatus == ContestRegistrationStatus.APPROVED) {
                ensureParticipantRecord(contestId, request.userId(), now);
            }
            ContestRegistration persisted = contestRegistrationMapper.selectById(toCreate.getId());
            return toRegistrationView(persisted != null ? persisted : toCreate, contestId);
        }

        ContestRegistrationStatus currentStatus = ContestRegistrationStatus.fromCode(existing.getStatus());
        if (currentStatus == ContestRegistrationStatus.APPROVED) {
            return toRegistrationView(existing, contestId);
        }
        existing.setSource(source.getCode());
        existing.setNote(StringUtils.hasText(request.note()) ? request.note() : existing.getNote());
        existing.setStatus(targetStatus.getCode());
        if (targetStatus == ContestRegistrationStatus.APPROVED) {
            assertParticipantCapacity(contest, 1);
            existing.setReviewedBy(operatorId);
            existing.setReviewedAt(now);
        } else {
            existing.setReviewedBy(null);
            existing.setReviewedAt(null);
        }
        contestRegistrationMapper.updateById(existing);
        if (targetStatus == ContestRegistrationStatus.APPROVED) {
            ensureParticipantRecord(contestId, request.userId(), now);
        }
        ContestRegistration persisted = contestRegistrationMapper.selectById(existing.getId());
        return toRegistrationView(persisted != null ? persisted : existing, contestId);
    }

    public List<ContestRegistrationView> decideRegistrations(
            Long contestId, ContestRegistrationDecisionRequest request, Long operatorId) {
        Contest contest = requireContest(contestId);
        ContestRegistrationStatus target = request.targetStatus() == null ? ContestRegistrationStatus.APPROVED
                : request.targetStatus();
        if (!EnumSet.of(ContestRegistrationStatus.APPROVED, ContestRegistrationStatus.REJECTED)
                .contains(target)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅支持审批或驳回操作");
        }

        List<ContestRegistration> registrations = contestRegistrationMapper.selectBatchIds(request.registrationIds());
        if (registrations.isEmpty()) {
            return List.of();
        }

        List<ContestRegistrationView> updated = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        List<ContestRegistration> toUpdate = new ArrayList<>();

        if (target == ContestRegistrationStatus.APPROVED) {
            long countNeedApprove = registrations.stream()
                    .filter(reg -> !ContestRegistrationStatus.APPROVED
                            .getCode()
                            .equalsIgnoreCase(reg.getStatus()))
                    .count();
            assertParticipantCapacity(contest, (int) countNeedApprove);
        }

        for (ContestRegistration registration : registrations) {
            if (!contestId.equals(registration.getContestId())) {
                continue;
            }
            ContestRegistrationStatus current = ContestRegistrationStatus.fromCode(registration.getStatus());
            if (current == target) {
                updated.add(toRegistrationView(registration, contestId));
                continue;
            }
            if (current.isFinal() && current != ContestRegistrationStatus.PENDING) {
                // 已终态且非待审核，跳过
                updated.add(toRegistrationView(registration, contestId));
                continue;
            }
            registration.setStatus(target.getCode());
            registration.setNote(StringUtils.hasText(request.note()) ? request.note() : registration.getNote());
            registration.setReviewedBy(operatorId);
            registration.setReviewedAt(now);
            toUpdate.add(registration);
            if (target == ContestRegistrationStatus.APPROVED) {
                ensureParticipantRecord(contestId, registration.getUserId(), now);
            } else if (target == ContestRegistrationStatus.REJECTED) {
                // 如果拒绝同时需要移除参赛者
                contestParticipantMapper.delete(
                        Wrappers.lambdaQuery(ContestParticipant.class)
                                .eq(ContestParticipant::getContestId, contestId)
                                .eq(ContestParticipant::getUserId, registration.getUserId()));
            }
            updated.add(toRegistrationView(registration, contestId));
        }

        if (!toUpdate.isEmpty()) {
            toUpdate.forEach(contestRegistrationMapper::updateById);
        }
        if (!toUpdate.isEmpty()) {
            List<Long> refreshIds = toUpdate.stream().map(ContestRegistration::getId).toList();
            List<ContestRegistration> refreshed = contestRegistrationMapper.selectBatchIds(refreshIds);
            Map<Long, ContestRegistration> refreshedMap = refreshed.stream()
                    .collect(Collectors.toMap(ContestRegistration::getId, reg -> reg));
            updated = updated.stream()
                    .map(view -> {
                        ContestRegistration refreshedEntity = refreshedMap.get(view.id());
                        return refreshedEntity != null ? toRegistrationView(refreshedEntity, contestId) : view;
                    })
                    .toList();
        }
        return updated;
    }

    @Transactional(readOnly = true)
    public List<ProblemSummaryOption> searchProblems(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int fetchSize = Math.max(limit, 1);
        List<ProblemStatement> statements = problemStatementMapper.selectList(
                Wrappers.lambdaQuery(ProblemStatement.class)
                        .eq(ProblemStatement::getLangCode, DEFAULT_STATEMENT_LANG)
                        .like(ProblemStatement::getTitle, keyword)
                        .last("LIMIT " + fetchSize));
        if (statements.isEmpty()) {
            return List.of();
        }
        Map<Long, Problem> problemMap = problemMapper.selectBatchIds(
                statements.stream().map(ProblemStatement::getProblemId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Problem::getId, problem -> problem));
        return statements.stream()
                .map(
                        statement -> {
                            Problem problem = problemMap.get(statement.getProblemId());
                            return new ProblemSummaryOption(
                                    statement.getProblemId(),
                                    problem != null ? problem.getSlug() : null,
                                    statement.getTitle(),
                                    problem != null ? problem.getProblemType() : null);
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryOption> searchUsers(String keyword, int limit) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> query = Wrappers.lambdaQuery(User.class)
                .like(User::getUsername, keyword)
                .or(q -> q.like(User::getEmail, keyword))
                .last("LIMIT " + Math.max(limit, 1));
        return userMapper.selectList(query).stream()
                .map(user -> new UserSummaryOption(user.getId(), user.getUsername(), user.getBio(), user.getEmail()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContestOptionsResponse loadOptions() {
        List<ContestKindOption> kindOptions = List.of(
                new ContestKindOption(ContestKind.ICPC.getCode(), ContestKind.ICPC.getDisplayName()),
                new ContestKindOption(ContestKind.OI.getCode(), ContestKind.OI.getDisplayName()),
                new ContestKindOption(ContestKind.IOI.getCode(), ContestKind.IOI.getDisplayName()),
                new ContestKindOption(ContestKind.CF.getCode(), ContestKind.CF.getDisplayName()),
                new ContestKindOption(ContestKind.ACM.getCode(), ContestKind.ACM.getDisplayName()),
                new ContestKindOption(ContestKind.CUSTOM.getCode(), ContestKind.CUSTOM.getDisplayName()));
        List<String> statuses = List.of(
                ContestStatus.UPCOMING.name(),
                ContestStatus.RUNNING.name(),
                ContestStatus.ENDED.name());
        List<String> registrationModes = List.of(
                ContestRegistrationMode.OPEN.getCode(),
                ContestRegistrationMode.APPROVAL.getCode(),
                ContestRegistrationMode.INVITE_ONLY.getCode());
        return new ContestOptionsResponse(kindOptions, statuses, registrationModes);
    }

    private ContestDetailView buildContestDetail(Contest contest) {
        ContestStatus status = inferStatus(contest, LocalDateTime.now());
        List<ContestProblemView> problems = loadContestProblems(contest.getId());
        List<ContestParticipantView> participants = loadContestParticipants(contest.getId());
        int pendingRegistrations = contestRegistrationMapper.selectCount(
                Wrappers.lambdaQuery(ContestRegistration.class)
                        .eq(ContestRegistration::getContestId, contest.getId())
                        .eq(ContestRegistration::getStatus, ContestRegistrationStatus.PENDING.getCode()))
                .intValue();
        return new ContestDetailView(
                contest.getId(),
                contest.getTitle(),
                contest.getDescriptionMd(),
                contest.getKind(),
                contest.getIsVisible() != null && contest.getIsVisible() == 1,
                status,
                contest.getStartTime(),
                contest.getEndTime(),
                contest.getRegistrationMode(),
                contest.getRegistrationStartTime(),
                contest.getRegistrationEndTime(),
                contest.getMaxParticipants(),
                contest.getPenaltyPerWrong(),
                contest.getScoreboardFreezeMinutes(),
                contest.getHideScoreDuringFreeze() != null && contest.getHideScoreDuringFreeze() == 1,
                contest.getCreatedBy(),
                contest.getCreatedAt(),
                contest.getUpdatedAt(),
                problems.size(),
                participants.size(),
                pendingRegistrations,
                problems,
                participants);
    }

    private void applyContestConfig(Contest contest, ContestUpsertRequest request) {
        ContestRegistrationMode mode = ContestRegistrationMode.OPEN;
        if (StringUtils.hasText(request.registrationMode())) {
            mode = ContestRegistrationMode.fromCode(request.registrationMode());
        }
        contest.setRegistrationMode(mode.getCode());
        contest.setRegistrationStartTime(request.registrationStartTime());
        contest.setRegistrationEndTime(request.registrationEndTime());
        contest.setMaxParticipants(request.maxParticipants());
        int penalty = request.penaltyPerWrong() != null && request.penaltyPerWrong() > 0
                ? request.penaltyPerWrong()
                : 20;
        contest.setPenaltyPerWrong(penalty);
        int freezeMinutes = request.scoreboardFreezeMinutes() != null && request.scoreboardFreezeMinutes() >= 0
                ? request.scoreboardFreezeMinutes()
                : 0;
        contest.setScoreboardFreezeMinutes(freezeMinutes);
        contest.setHideScoreDuringFreeze(Boolean.TRUE.equals(request.hideScoreDuringFreeze()) ? 1 : 0);
    }

    private List<ContestProblemView> loadContestProblems(Long contestId) {
        List<ContestProblem> relations = contestProblemMapper.selectList(
                Wrappers.lambdaQuery(ContestProblem.class)
                        .eq(ContestProblem::getContestId, contestId)
                        .orderByAsc(ContestProblem::getOrderNo, ContestProblem::getProblemId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> problemIds = relations.stream().map(ContestProblem::getProblemId).toList();
        Map<Long, Problem> problemMap = problemMapper
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
        List<ProblemStatement> statements = problemStatementMapper.selectList(
                Wrappers.lambdaQuery(ProblemStatement.class)
                        .in(ProblemStatement::getProblemId, problemIds)
                        .eq(ProblemStatement::getLangCode, DEFAULT_STATEMENT_LANG));
        return statements.stream()
                .collect(Collectors.toMap(ProblemStatement::getProblemId, statement -> statement,
                        (left, right) -> left));
    }

    private Map<Long, ProblemStatsView> loadProblemStats(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        List<ProblemStatsView> stats = problemStatsViewMapper.selectList(
                Wrappers.lambdaQuery(ProblemStatsView.class)
                        .in(ProblemStatsView::getProblemId, problemIds));
        return stats.stream()
                .collect(Collectors.toMap(ProblemStatsView::getProblemId, view -> view, (left, right) -> left));
    }

    private List<ContestParticipantView> loadContestParticipants(Long contestId) {
        List<ContestParticipant> relations = contestParticipantMapper.selectList(
                Wrappers.lambdaQuery(ContestParticipant.class)
                        .eq(ContestParticipant::getContestId, contestId)
                        .orderByAsc(ContestParticipant::getRegisteredAt));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = relations.stream().map(ContestParticipant::getUserId).toList();
        Map<Long, User> userMap = userMapper
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

    private Contest requireContest(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "比赛不存在");
        }
        return contest;
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
        QueryWrapper<ContestProblem> query = Wrappers.query();
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

    private ContestRegistrationView toRegistrationView(ContestRegistration registration, Long contestId) {
        User user = userMapper.selectById(registration.getUserId());
        User reviewer = registration.getReviewedBy() == null ? null
                : userMapper.selectById(registration.getReviewedBy());
        return new ContestRegistrationView(
                registration.getId(),
                contestId,
                registration.getUserId(),
                user != null ? user.getUsername() : null,
                user != null ? user.getBio() : null,
                ContestRegistrationStatus.fromCode(registration.getStatus()),
                registration.getSource(),
                registration.getNote(),
                registration.getReviewedBy(),
                reviewer != null ? reviewer.getUsername() : null,
                registration.getReviewedAt(),
                registration.getCreatedAt());
    }

    private void ensureParticipantRecord(Long contestId, Long userId, LocalDateTime registeredAt) {
        ContestParticipant existing = contestParticipantMapper.selectOne(
                Wrappers.lambdaQuery(ContestParticipant.class)
                        .eq(ContestParticipant::getContestId, contestId)
                        .eq(ContestParticipant::getUserId, userId));
        if (existing != null) {
            return;
        }
        ContestParticipant participant = new ContestParticipant();
        participant.setContestId(contestId);
        participant.setUserId(userId);
        participant.setRegisteredAt(registeredAt != null ? registeredAt : LocalDateTime.now());
        contestParticipantMapper.insert(participant);
    }

    private void assertParticipantCapacity(Contest contest, int additionalParticipants) {
        if (contest.getMaxParticipants() == null || additionalParticipants <= 0) {
            return;
        }
        int currentCount = contestParticipantMapper.selectCount(
                Wrappers.lambdaQuery(ContestParticipant.class)
                        .eq(ContestParticipant::getContestId, contest.getId()))
                .intValue();
        if (currentCount + additionalParticipants > contest.getMaxParticipants()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "参赛人数超过上限");
        }
    }

    private void upsertApprovedRegistration(
            Long contestId,
            Long userId,
            Long operatorId,
            ContestRegistration existing,
            String fallbackNote,
            boolean ensureParticipant) {
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setStatus(ContestRegistrationStatus.APPROVED.getCode());
            existing.setReviewedBy(operatorId);
            existing.setReviewedAt(now);
            if (!StringUtils.hasText(existing.getNote()) && StringUtils.hasText(fallbackNote)) {
                existing.setNote(fallbackNote);
            }
            contestRegistrationMapper.updateById(existing);
            if (ensureParticipant) {
                ensureParticipantRecord(contestId, userId, now);
            }
            return;
        }
        ContestRegistration registration = new ContestRegistration();
        registration.setContestId(contestId);
        registration.setUserId(userId);
        registration.setStatus(ContestRegistrationStatus.APPROVED.getCode());
        registration.setSource(ContestRegistrationSource.ADMIN.getCode());
        registration.setNote(fallbackNote);
        registration.setReviewedBy(operatorId);
        registration.setReviewedAt(now);
        contestRegistrationMapper.insert(registration);
        if (ensureParticipant) {
            ensureParticipantRecord(contestId, userId, now);
        }
    }
}
