package com.david.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.judge.dto.JudgeJobDetailView;
import com.david.judge.dto.JudgeJobQuery;
import com.david.judge.dto.JudgeJobView;
import com.david.judge.dto.LanguageSummary;
import com.david.judge.dto.NodeSummary;
import com.david.judge.dto.PageResult;
import com.david.judge.dto.ProblemSummary;
import com.david.judge.dto.SubmissionArtifactView;
import com.david.judge.dto.SubmissionSummary;
import com.david.judge.dto.SubmissionTestView;
import com.david.judge.dto.TestSummary;
import com.david.judge.dto.UserSummary;
import com.david.judge.entity.FileRecord;
import com.david.judge.entity.JudgeJob;
import com.david.judge.entity.JudgeNode;
import com.david.judge.entity.Language;
import com.david.judge.entity.Problem;
import com.david.judge.entity.Submission;
import com.david.judge.entity.SubmissionArtifact;
import com.david.judge.entity.SubmissionTest;
import com.david.judge.entity.Testcase;
import com.david.judge.entity.TestcaseGroup;
import com.david.judge.entity.User;
import com.david.core.exception.BusinessException;
import com.david.judge.mapper.FileRecordMapper;
import com.david.judge.mapper.JudgeJobMapper;
import com.david.judge.mapper.JudgeNodeMapper;
import com.david.judge.mapper.LanguageMapper;
import com.david.judge.mapper.ProblemMapper;
import com.david.judge.mapper.SubmissionArtifactMapper;
import com.david.judge.mapper.SubmissionMapper;
import com.david.judge.mapper.SubmissionTestMapper;
import com.david.judge.mapper.TestcaseGroupMapper;
import com.david.judge.mapper.TestcaseMapper;
import com.david.judge.mapper.UserMapper;
import com.david.judge.mapper.model.SubmissionArtifactAggregate;
import com.david.judge.mapper.model.SubmissionTestAggregate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class JudgeJobService {

    private final JudgeJobMapper judgeJobMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionTestMapper submissionTestMapper;
    private final SubmissionArtifactMapper submissionArtifactMapper;
    private final FileRecordMapper fileRecordMapper;
    private final JudgeNodeMapper judgeNodeMapper;
    private final UserMapper userMapper;
    private final ProblemMapper problemMapper;
    private final LanguageMapper languageMapper;
    private final TestcaseMapper testcaseMapper;
    private final TestcaseGroupMapper testcaseGroupMapper;

    public PageResult<JudgeJobView> pageJobs(JudgeJobQuery query) {
        LambdaQueryWrapper<JudgeJob> wrapper = Wrappers.lambdaQuery(JudgeJob.class);
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(JudgeJob::getStatus, query.status().trim());
        }
        if (query.onlyUnassigned()) {
            wrapper.isNull(JudgeJob::getNodeId);
        } else if (query.nodeId() != null) {
            wrapper.eq(JudgeJob::getNodeId, query.nodeId());
        }
        if (query.submissionId() != null) {
            wrapper.eq(JudgeJob::getSubmissionId, query.submissionId());
        }

        List<Long> keywordSubmissionIds = null;
        if (StringUtils.hasText(query.keyword())) {
            String trimmed = query.keyword().trim();
            Long numeric = parseLong(trimmed);
            if (numeric != null) {
                wrapper.and(
                        w -> w.eq(JudgeJob::getId, numeric)
                                .or()
                                .eq(JudgeJob::getSubmissionId, numeric));
            } else {
                keywordSubmissionIds = submissionMapper.searchSubmissionIdsByKeyword(trimmed, 500);
                if (CollectionUtils.isEmpty(keywordSubmissionIds)) {
                    return new PageResult<>(List.of(), 0, query.page(), query.size());
                }
            }
        }
        if (!CollectionUtils.isEmpty(keywordSubmissionIds)) {
            wrapper.in(JudgeJob::getSubmissionId, keywordSubmissionIds);
        }

        wrapper.orderByDesc(JudgeJob::getCreatedAt);
        Page<JudgeJob> pager = new Page<>(query.page(), query.size());
        Page<JudgeJob> result = judgeJobMapper.selectPage(pager, wrapper);
        List<JudgeJob> records = result.getRecords();
        List<JudgeJobView> views = hydrateJobs(records);
        return new PageResult<>(views, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public JudgeJobDetailView getJobDetail(Long jobId) {
        JudgeJob job = judgeJobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "任务不存在");
        }
        List<JudgeJobView> jobs = hydrateJobs(List.of(job));
        JudgeJobView jobView = jobs.isEmpty()
                ? new JudgeJobView(
                        job.getId(),
                        job.getSubmissionId(),
                        job.getStatus(),
                        job.getPriority(),
                        job.getCreatedAt(),
                        job.getStartedAt(),
                        job.getFinishedAt(),
                        null,
                        null,
                        false,
                        new TestSummary(0, 0, 0))
                : jobs.get(0);
        List<SubmissionTestView> tests = loadSubmissionTests(job.getSubmissionId());
        List<SubmissionArtifactView> artifacts = loadSubmissionArtifacts(job.getSubmissionId());
        return new JudgeJobDetailView(jobView, tests, artifacts);
    }

    public void retryJob(Long jobId) {
        JudgeJob job = judgeJobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "任务不存在");
        }
        if (!"failed".equals(job.getStatus()) && !"canceled".equals(job.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "仅失败或已取消的任务可重试");
        }
        LambdaUpdateWrapper<JudgeJob> update = new LambdaUpdateWrapper<JudgeJob>()
                .eq(JudgeJob::getId, jobId)
                .set(JudgeJob::getStatus, "queued")
                .set(JudgeJob::getNodeId, null)
                .set(JudgeJob::getStartedAt, null)
                .set(JudgeJob::getFinishedAt, null);
        int affected = judgeJobMapper.update(null, update);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "任务状态已变化，请刷新后重试");
        }
    }

    private List<JudgeJobView> hydrateJobs(List<JudgeJob> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }
        Set<Long> submissionIds = jobs.stream().map(JudgeJob::getSubmissionId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Submission> submissions = fetchAsMap(submissionMapper.selectBatchIds(submissionIds),
                Submission::getId);
        Map<Long, User> users = fetchAsMap(
                userMapper.selectBatchIds(
                        submissions.values().stream()
                                .map(Submission::getUserId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet())),
                User::getId);
        Map<Long, Problem> problems = fetchAsMap(
                problemMapper.selectBatchIds(
                        submissions.values().stream()
                                .map(Submission::getProblemId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet())),
                Problem::getId);
        Map<Integer, Language> languages = fetchAsMap(
                languageMapper.selectBatchIds(
                        submissions.values().stream()
                                .map(Submission::getLanguageId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet())),
                Language::getId);
        Map<Long, JudgeNode> nodes = fetchAsMap(
                judgeNodeMapper.selectBatchIds(
                        jobs.stream().map(JudgeJob::getNodeId).filter(Objects::nonNull).collect(Collectors.toSet())),
                JudgeNode::getId);
        Map<Long, TestSummary> testSummaries = buildTestSummaries(submissionIds);
        Map<Long, Boolean> artifactFlags = buildArtifactFlags(submissionIds);

        List<JudgeJobView> views = new ArrayList<>(jobs.size());
        for (JudgeJob job : jobs) {
            Submission submission = submissions.get(job.getSubmissionId());
            SubmissionSummary submissionSummary = submission == null
                    ? null
                    : new SubmissionSummary(
                            submission.getId(),
                            submission.getVerdict(),
                            submission.getScore(),
                            submission.getTimeMs(),
                            submission.getMemoryKb(),
                            submission.getCodeBytes(),
                            submission.getCreatedAt(),
                            buildUserSummary(users.get(submission.getUserId())),
                            buildProblemSummary(problems.get(submission.getProblemId())),
                            buildLanguageSummary(languages.get(submission.getLanguageId())));
            NodeSummary nodeSummary = buildNodeSummary(nodes.get(job.getNodeId()));
            TestSummary testSummary = testSummaries.getOrDefault(job.getSubmissionId(), new TestSummary(0, 0, 0));
            boolean hasArtifacts = artifactFlags.getOrDefault(job.getSubmissionId(), Boolean.FALSE);
            views.add(
                    new JudgeJobView(
                            job.getId(),
                            job.getSubmissionId(),
                            job.getStatus(),
                            job.getPriority(),
                            job.getCreatedAt(),
                            job.getStartedAt(),
                            job.getFinishedAt(),
                            nodeSummary,
                            submissionSummary,
                            hasArtifacts,
                            testSummary));
        }
        return views;
    }

    private Map<Long, TestSummary> buildTestSummaries(Set<Long> submissionIds) {
        if (CollectionUtils.isEmpty(submissionIds)) {
            return Map.of();
        }
        List<SubmissionTestAggregate> aggregates = submissionTestMapper.aggregateTests(submissionIds);
        Map<Long, TestSummary> result = new HashMap<>();
        for (SubmissionTestAggregate aggregate : aggregates) {
            int total = aggregate.total() == null ? 0 : aggregate.total();
            int passed = aggregate.passed() == null ? 0 : aggregate.passed();
            int failed = Math.max(total - passed, 0);
            result.put(aggregate.submissionId(), new TestSummary(total, passed, failed));
        }
        return result;
    }

    private Map<Long, Boolean> buildArtifactFlags(Set<Long> submissionIds) {
        if (CollectionUtils.isEmpty(submissionIds)) {
            return Map.of();
        }
        List<SubmissionArtifactAggregate> aggregates = submissionArtifactMapper.aggregateArtifacts(submissionIds);
        Map<Long, Boolean> result = new HashMap<>();
        for (SubmissionArtifactAggregate aggregate : aggregates) {
            result.put(aggregate.submissionId(), (aggregate.count() != null && aggregate.count() > 0));
        }
        return result;
    }

    private List<SubmissionTestView> loadSubmissionTests(Long submissionId) {
        if (submissionId == null) {
            return List.of();
        }
        List<SubmissionTest> tests = submissionTestMapper.selectList(
                Wrappers.lambdaQuery(SubmissionTest.class)
                        .eq(SubmissionTest::getSubmissionId, submissionId)
                        .orderByAsc(SubmissionTest::getId));
        if (tests.isEmpty()) {
            return List.of();
        }
        Map<Long, Testcase> testcases = fetchAsMap(
                testcaseMapper.selectBatchIds(
                        tests.stream().map(SubmissionTest::getTestcaseId).filter(Objects::nonNull)
                                .collect(Collectors.toSet())),
                Testcase::getId);
        Map<Long, TestcaseGroup> groups = fetchAsMap(
                testcaseGroupMapper.selectBatchIds(
                        tests.stream().map(SubmissionTest::getGroupId).filter(Objects::nonNull)
                                .collect(Collectors.toSet())),
                TestcaseGroup::getId);
        return tests.stream()
                .map(
                        test -> {
                            Testcase testcase = testcases.get(test.getTestcaseId());
                            TestcaseGroup group = groups.get(test.getGroupId());
                            return new SubmissionTestView(
                                    test.getId(),
                                    test.getTestcaseId(),
                                    test.getGroupId(),
                                    group == null ? null : group.getName(),
                                    group != null && Boolean.TRUE.equals(group.getSample()),
                                    testcase == null ? null : testcase.getOrderIndex(),
                                    test.getVerdict(),
                                    test.getTimeMs(),
                                    test.getMemoryKb(),
                                    test.getScore(),
                                    test.getMessage());
                        })
                .toList();
    }

    private List<SubmissionArtifactView> loadSubmissionArtifacts(Long submissionId) {
        if (submissionId == null) {
            return List.of();
        }
        List<SubmissionArtifact> artifacts = submissionArtifactMapper.selectList(
                Wrappers.lambdaQuery(SubmissionArtifact.class)
                        .eq(SubmissionArtifact::getSubmissionId, submissionId)
                        .orderByAsc(SubmissionArtifact::getCreatedAt));
        if (artifacts.isEmpty()) {
            return List.of();
        }
        Map<Long, FileRecord> files = fetchAsMap(
                fileRecordMapper.selectBatchIds(
                        artifacts.stream().map(SubmissionArtifact::getFileId).filter(Objects::nonNull)
                                .collect(Collectors.toSet())),
                FileRecord::getId);
        return artifacts.stream()
                .map(
                        artifact -> {
                            FileRecord file = files.get(artifact.getFileId());
                            return new SubmissionArtifactView(
                                    artifact.getId(),
                                    artifact.getSubmissionId(),
                                    artifact.getKind(),
                                    artifact.getFileId(),
                                    file == null ? null : file.getStorageKey(),
                                    file == null ? null : file.getSha256(),
                                    file == null ? null : file.getMimeType(),
                                    file == null ? null : file.getSizeBytes(),
                                    artifact.getCreatedAt());
                        })
                .toList();
    }

    private NodeSummary buildNodeSummary(JudgeNode node) {
        if (node == null) {
            return null;
        }
        return new NodeSummary(node.getId(), node.getName(), node.getStatus());
    }

    private UserSummary buildUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(user.getId(), user.getUsername(), user.getEmail());
    }

    private ProblemSummary buildProblemSummary(Problem problem) {
        if (problem == null) {
            return null;
        }
        return new ProblemSummary(problem.getId(), problem.getSlug());
    }

    private LanguageSummary buildLanguageSummary(Language language) {
        if (language == null) {
            return null;
        }
        return new LanguageSummary(language.getId(), language.getCode(), language.getDisplayName());
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private <T, K> Map<K, T> fetchAsMap(List<T> items, Function<T, K> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(keyExtractor, Function.identity(), (a, b) -> a));
    }
}
