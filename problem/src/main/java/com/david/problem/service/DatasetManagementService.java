package com.david.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.core.exception.BusinessException;
import com.david.problem.dto.DatasetDetailView;
import com.david.problem.dto.DatasetUpsertRequest;
import com.david.problem.dto.TestcaseGroupUpsertRequest;
import com.david.problem.dto.TestcaseGroupView;
import com.david.problem.dto.TestcaseUpsertRequest;
import com.david.problem.dto.TestcaseView;
import com.david.problem.entity.Dataset;
import com.david.problem.entity.Problem;
import com.david.problem.entity.Testcase;
import com.david.problem.entity.TestcaseGroup;
import com.david.problem.mapper.DatasetMapper;
import com.david.problem.mapper.ProblemMapper;
import com.david.problem.mapper.TestcaseGroupMapper;
import com.david.problem.mapper.TestcaseMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Nullable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatasetManagementService {

    private static final Set<String> CHECKER_TYPES = Set.of("text", "float", "custom");
    private static final String REVIEW_APPROVED = "approved";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_READY = "ready";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ARCHIVED = "archived";
    private static final String STATUS_DRAFT = "draft";

    private final DatasetMapper datasetMapper;
    private final TestcaseGroupMapper testcaseGroupMapper;
    private final TestcaseMapper testcaseMapper;
    private final ProblemMapper problemMapper;
    private final ObjectMapper objectMapper;

    public DatasetManagementService(
            DatasetMapper datasetMapper,
            TestcaseGroupMapper testcaseGroupMapper,
            TestcaseMapper testcaseMapper,
            ProblemMapper problemMapper,
            ObjectMapper objectMapper) {
        this.datasetMapper = datasetMapper;
        this.testcaseGroupMapper = testcaseGroupMapper;
        this.testcaseMapper = testcaseMapper;
        this.problemMapper = problemMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DatasetDetailView> listProblemDatasets(Long problemId) {
        ensureProblemExists(problemId);

        List<Dataset> datasets =
                datasetMapper.selectList(
                        Wrappers.lambdaQuery(Dataset.class)
                                .eq(Dataset::getProblemId, problemId)
                                .orderByDesc(Dataset::getIsActive)
                                .orderByAsc(Dataset::getId));
        if (CollectionUtils.isEmpty(datasets)) {
            return List.of();
        }

        // 加载所有分组与用例并按数据集聚合，再构建视图
        List<Long> datasetIds = datasets.stream().map(Dataset::getId).toList();
        List<TestcaseGroup> allGroups = loadGroupsByDatasetIds(datasetIds);
        Map<Long, List<TestcaseGroup>> groupsByDataset =
                allGroups.stream().collect(Collectors.groupingBy(TestcaseGroup::getDatasetId));

        Map<Long, List<Testcase>> testcasesByGroup =
                loadTestcasesByGroupIds(allGroups.stream().map(TestcaseGroup::getId).toList());

        return datasets.stream()
                .map(
                        ds ->
                                toDatasetDetailView(
                                        ds,
                                        toGroupViews(
                                                allGroupsOf(ds.getId(), groupsByDataset),
                                                testcasesByGroup,
                                                true)))
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetDetailView getDataset(Long problemId, Long datasetId) {
        Dataset dataset = getDatasetOrThrow(datasetId);
        if (!Objects.equals(dataset.getProblemId(), problemId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "数据集不存在");
        }

        List<TestcaseGroup> groups = loadGroupsByDatasetId(datasetId);
        Map<Long, List<Testcase>> testcasesByGroup =
                loadTestcasesByGroupIds(groups.stream().map(TestcaseGroup::getId).toList());

        return toDatasetDetailView(dataset, toGroupViews(groups, testcasesByGroup, true));
    }

    @Transactional
    public DatasetDetailView createDataset(Long problemId, DatasetUpsertRequest request) {
        ensureProblemExists(problemId);
        String name = normalizeName(request.name());
        ensureDatasetNameUnique(problemId, name, null);

        Dataset dataset = new Dataset();
        dataset.setProblemId(problemId);
        dataset.setName(name);

        boolean active = Boolean.TRUE.equals(request.isActive());
        dataset.setIsActive(active ? 1 : 0);

        applyCheckerConfig(dataset, request);

        dataset.setCreatedAt(LocalDateTime.now());
        dataset.setUpdatedAt(LocalDateTime.now());
        datasetMapper.insert(dataset);

        if (active) {
            activateDataset(problemId, dataset.getId());
        }
        return getDataset(problemId, dataset.getId());
    }

    @Transactional
    public DatasetDetailView updateDataset(
            Long problemId, Long datasetId, DatasetUpsertRequest request) {
        Dataset existing = getDatasetOrThrow(datasetId);
        if (!Objects.equals(existing.getProblemId(), problemId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "数据集不存在");
        }
        String name = normalizeName(request.name());
        ensureDatasetNameUnique(problemId, name, datasetId);

        boolean wasActive = existing.getIsActive() != null && existing.getIsActive() == 1;
        boolean active = Boolean.TRUE.equals(request.isActive());

        existing.setName(name);
        existing.setIsActive(active ? 1 : 0);
        applyCheckerConfig(existing, request);
        existing.setUpdatedAt(LocalDateTime.now());
        datasetMapper.updateById(existing);

        if (active) {
            activateDataset(problemId, datasetId);
        } else if (wasActive) {
            clearActiveDataset(problemId, datasetId);
        }
        return getDataset(problemId, datasetId);
    }

    @Transactional
    public void deleteDataset(Long problemId, Long datasetId) {
        Dataset existing = getDatasetOrThrow(datasetId);
        if (!Objects.equals(existing.getProblemId(), problemId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "数据集不存在");
        }
        datasetMapper.deleteById(datasetId);

        if (existing.getIsActive() != null && existing.getIsActive() == 1) {
            clearActiveDataset(problemId, datasetId);
        }
    }

    @Transactional(readOnly = true)
    public List<TestcaseGroupView> listGroups(Long datasetId, boolean includeTestcases) {
        Dataset dataset = getDatasetOrThrow(datasetId);

        List<TestcaseGroup> groups = loadGroupsByDatasetId(dataset.getId());
        if (CollectionUtils.isEmpty(groups)) {
            return List.of();
        }

        Map<Long, List<Testcase>> testcaseMap =
                includeTestcases
                        ? loadTestcasesByGroupIds(
                                groups.stream().map(TestcaseGroup::getId).toList())
                        : Map.of();

        return toGroupViews(groups, testcaseMap, includeTestcases);
    }

    @Transactional(readOnly = true)
    public TestcaseGroupView getGroup(Long datasetId, Long groupId, boolean includeTestcases) {
        TestcaseGroup group = getGroupOrThrow(groupId);
        if (!Objects.equals(group.getDatasetId(), datasetId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试组不存在");
        }

        List<Testcase> testcases =
                includeTestcases
                        ? loadTestcasesByGroupIds(List.of(groupId)).getOrDefault(groupId, List.of())
                        : List.of();

        return toGroupView(group, testcases);
    }

    @Transactional
    public TestcaseGroupView createGroup(Long datasetId, TestcaseGroupUpsertRequest request) {
        Dataset dataset = getDatasetOrThrow(datasetId);
        String name = normalizeName(request.name());

        TestcaseGroup group = new TestcaseGroup();
        group.setDatasetId(dataset.getId());
        group.setName(name);
        group.setIsSample(Boolean.TRUE.equals(request.isSample()) ? 1 : 0);
        group.setWeight(request.weight());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        testcaseGroupMapper.insert(group);
        return getGroup(datasetId, group.getId(), true);
    }

    @Transactional
    public TestcaseGroupView updateGroup(
            Long datasetId, Long groupId, TestcaseGroupUpsertRequest request) {
        TestcaseGroup existing = getGroupOrThrow(groupId);
        if (!Objects.equals(existing.getDatasetId(), datasetId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试组不存在");
        }
        existing.setName(normalizeName(request.name()));
        existing.setIsSample(Boolean.TRUE.equals(request.isSample()) ? 1 : 0);
        existing.setWeight(request.weight());
        existing.setUpdatedAt(LocalDateTime.now());
        testcaseGroupMapper.updateById(existing);
        return getGroup(datasetId, groupId, true);
    }

    @Transactional
    public void deleteGroup(Long datasetId, Long groupId) {
        TestcaseGroup existing = getGroupOrThrow(groupId);
        if (!Objects.equals(existing.getDatasetId(), datasetId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试组不存在");
        }
        testcaseGroupMapper.deleteById(groupId);
    }

    @Transactional(readOnly = true)
    public List<TestcaseView> listTestcases(Long groupId) {
        TestcaseGroup group = getGroupOrThrow(groupId);
        List<Testcase> testcases =
                testcaseMapper.selectList(
                        Wrappers.lambdaQuery(Testcase.class)
                                .eq(Testcase::getGroupId, group.getId())
                                .orderByAsc(Testcase::getOrderIndex)
                                .orderByAsc(Testcase::getId));
        if (CollectionUtils.isEmpty(testcases)) {
            return List.of();
        }
        return testcases.stream().map(this::toTestcaseView).toList();
    }

    @Transactional(readOnly = true)
    public TestcaseView getTestcase(Long groupId, Long testcaseId) {
        Testcase testcase = getTestcaseOrThrow(testcaseId);
        if (!Objects.equals(testcase.getGroupId(), groupId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试用例不存在");
        }
        return toTestcaseView(testcase);
    }

    @Transactional
    public TestcaseView createTestcase(Long groupId, TestcaseUpsertRequest request) {
        TestcaseGroup group = getGroupOrThrow(groupId);
        Testcase testcase = new Testcase();
        testcase.setGroupId(group.getId());
        applyTestcaseFields(testcase, request);
        testcase.setCreatedAt(LocalDateTime.now());
        testcase.setUpdatedAt(LocalDateTime.now());
        testcaseMapper.insert(testcase);
        return toTestcaseView(testcaseMapper.selectById(testcase.getId()));
    }

    @Transactional
    public TestcaseView updateTestcase(
            Long groupId, Long testcaseId, TestcaseUpsertRequest request) {
        Testcase existing = getTestcaseOrThrow(testcaseId);
        if (!Objects.equals(existing.getGroupId(), groupId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试用例不存在");
        }
        applyTestcaseFields(existing, request);
        existing.setUpdatedAt(LocalDateTime.now());
        testcaseMapper.updateById(existing);
        return toTestcaseView(existing);
    }

    @Transactional
    public void deleteTestcase(Long groupId, Long testcaseId) {
        Testcase existing = getTestcaseOrThrow(testcaseId);
        if (!Objects.equals(existing.getGroupId(), groupId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试用例不存在");
        }
        testcaseMapper.deleteById(testcaseId);
    }

    private void ensureProblemExists(Long problemId) {
        if (problemMapper.selectById(problemId) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }
    }

    private Dataset getDatasetOrThrow(Long datasetId) {
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "数据集不存在");
        }
        return dataset;
    }

    private TestcaseGroup getGroupOrThrow(Long groupId) {
        TestcaseGroup group = testcaseGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试组不存在");
        }
        return group;
    }

    private Testcase getTestcaseOrThrow(Long testcaseId) {
        Testcase testcase = testcaseMapper.selectById(testcaseId);
        if (testcase == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "测试用例不存在");
        }
        return testcase;
    }

    private void ensureDatasetNameUnique(Long problemId, String name, @Nullable Long excludeId) {
        LambdaQueryWrapper<Dataset> query =
                Wrappers.lambdaQuery(Dataset.class)
                        .eq(Dataset::getProblemId, problemId)
                        .eq(Dataset::getName, name);
        if (excludeId != null) {
            query.ne(Dataset::getId, excludeId);
        }
        Long count = datasetMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "同一题目下数据集名称不能重复");
        }
    }

    private void applyCheckerConfig(Dataset dataset, DatasetUpsertRequest request) {
        String checkerType = normalizeCheckerType(request.checkerType());
        dataset.setCheckerType(checkerType);
        Long checkerFileId = request.checkerFileId();
        if (!"custom".equals(checkerType)) {
            if (checkerFileId != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "只有自定义校验器才可指定校验器文件");
            }
            dataset.setCheckerFileId(null);
        } else {
            dataset.setCheckerFileId(checkerFileId);
        }

        if ("float".equals(checkerType)) {
            dataset.setFloatAbsTol(request.floatAbsTol());
            dataset.setFloatRelTol(request.floatRelTol());
        } else {
            dataset.setFloatAbsTol(null);
            dataset.setFloatRelTol(null);
        }
    }

    private void applyTestcaseFields(Testcase testcase, TestcaseUpsertRequest request) {
        testcase.setOrderIndex(request.orderIndex());
        testcase.setInputFileId(request.inputFileId());
        testcase.setOutputFileId(request.outputFileId());
        testcase.setInputJson(normalizeJson(request.inputJson(), "输入 JSON"));
        testcase.setOutputJson(normalizeJson(request.outputJson(), "输出 JSON"));
        testcase.setOutputType(
                StringUtils.hasText(request.outputType())
                        ? request.outputType().trim().toLowerCase(Locale.ROOT)
                        : null);
        testcase.setScore(request.score());
    }

    private String normalizeJson(@Nullable String json, String field) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, field + " 不是合法 JSON");
        }
    }

    private String normalizeName(String raw) {
        if (raw == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "名称不能为空");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "名称不能为空");
        }
        return trimmed;
    }

    private String normalizeCheckerType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "text";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (!CHECKER_TYPES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的校验器类型");
        }
        return normalized;
    }

    private void activateDataset(Long problemId, Long datasetId) {
        Problem existingProblem = problemMapper.selectById(problemId);
        if (existingProblem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        if (!REVIEW_APPROVED.equals(existingProblem.getReviewStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目尚未通过审核，无法激活数据集");
        }
        if (STATUS_ARCHIVED.equals(existingProblem.getLifecycleStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "已归档题目无法激活数据集");
        }

        LambdaUpdateWrapper<Dataset> deactivateOthers =
                Wrappers.lambdaUpdate(Dataset.class)
                        .eq(Dataset::getProblemId, problemId)
                        .ne(Dataset::getId, datasetId)
                        .set(Dataset::getIsActive, 0);
        datasetMapper.update(null, deactivateOthers);

        Dataset target = datasetMapper.selectById(datasetId);
        if (target != null && (target.getIsActive() == null || target.getIsActive() == 0)) {
            target.setIsActive(1);
            target.setUpdatedAt(LocalDateTime.now());
            datasetMapper.updateById(target);
        }

        Problem problem = new Problem();
        problem.setId(problemId);
        problem.setActiveDatasetId(datasetId);
        if (!STATUS_PUBLISHED.equals(existingProblem.getLifecycleStatus())) {
            problem.setLifecycleStatus(STATUS_READY);
        }
        problemMapper.updateById(problem);
    }

    private void clearActiveDataset(Long problemId, Long datasetId) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem != null && Objects.equals(problem.getActiveDatasetId(), datasetId)) {
            Problem update = new Problem();
            update.setId(problemId);
            update.setActiveDatasetId(null);
            update.setIsPublic(0);
            if (!STATUS_ARCHIVED.equals(problem.getLifecycleStatus())) {
                if (REVIEW_APPROVED.equals(problem.getReviewStatus())) {
                    update.setLifecycleStatus(STATUS_APPROVED);
                } else {
                    update.setLifecycleStatus(STATUS_DRAFT);
                }
            }
            problemMapper.updateById(update);
        }
    }

    /** 统一加载指定数据集的所有分组（含排序规则） */
    private List<TestcaseGroup> loadGroupsByDatasetId(Long datasetId) {
        return testcaseGroupMapper.selectList(
                Wrappers.lambdaQuery(TestcaseGroup.class)
                        .eq(TestcaseGroup::getDatasetId, datasetId)
                        .orderByDesc(TestcaseGroup::getIsSample)
                        .orderByAsc(TestcaseGroup::getWeight)
                        .orderByAsc(TestcaseGroup::getId));
    }

    /** 统一批量加载多个数据集下的所有分组（含排序规则） */
    private List<TestcaseGroup> loadGroupsByDatasetIds(List<Long> datasetIds) {
        if (CollectionUtils.isEmpty(datasetIds)) return List.of();
        return testcaseGroupMapper.selectList(
                Wrappers.lambdaQuery(TestcaseGroup.class)
                        .in(TestcaseGroup::getDatasetId, datasetIds)
                        .orderByDesc(TestcaseGroup::getIsSample)
                        .orderByAsc(TestcaseGroup::getWeight)
                        .orderByAsc(TestcaseGroup::getId));
    }

    /** 统一批量加载多个分组下的用例（含排序规则），返回按 groupId 分组的 Map */
    private Map<Long, List<Testcase>> loadTestcasesByGroupIds(List<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) return Map.of();
        List<Testcase> testcases =
                testcaseMapper.selectList(
                        Wrappers.lambdaQuery(Testcase.class)
                                .in(Testcase::getGroupId, groupIds)
                                .orderByAsc(Testcase::getOrderIndex)
                                .orderByAsc(Testcase::getId));
        if (CollectionUtils.isEmpty(testcases)) return Map.of();
        return testcases.stream()
                .collect(
                        Collectors.groupingBy(
                                Testcase::getGroupId, LinkedHashMap::new, Collectors.toList()));
    }

    /** 从 map 中取某数据集的分组列表，若无则返回空列表 */
    private List<TestcaseGroup> allGroupsOf(
            Long datasetId, Map<Long, List<TestcaseGroup>> groupsByDataset) {
        return groupsByDataset.getOrDefault(datasetId, List.of());
    }

    /** 批量把分组转成视图，按 includeTestcases 决定是否附带用例 */
    private List<TestcaseGroupView> toGroupViews(
            List<TestcaseGroup> groups,
            Map<Long, List<Testcase>> testcasesByGroup,
            boolean includeTestcases) {
        if (CollectionUtils.isEmpty(groups)) return List.of();
        Map<Long, List<Testcase>> safeMap = testcasesByGroup == null ? Map.of() : testcasesByGroup;
        return groups.stream()
                .map(
                        g ->
                                toGroupView(
                                        g,
                                        includeTestcases
                                                ? safeMap.getOrDefault(g.getId(), List.of())
                                                : List.of()))
                .toList();
    }

    private DatasetDetailView toDatasetDetailView(Dataset dataset, List<TestcaseGroupView> groups) {
        List<TestcaseGroupView> safeGroups = groups == null ? List.of() : List.copyOf(groups);
        return new DatasetDetailView(
                dataset.getId(),
                dataset.getProblemId(),
                dataset.getName(),
                dataset.getIsActive() != null && dataset.getIsActive() == 1,
                dataset.getCheckerType(),
                dataset.getCheckerFileId(),
                dataset.getFloatAbsTol(),
                dataset.getFloatRelTol(),
                dataset.getCreatedBy(),
                dataset.getCreatedAt(),
                dataset.getUpdatedAt(),
                safeGroups);
    }

    private TestcaseGroupView toGroupView(TestcaseGroup group, @Nullable List<Testcase> testcases) {
        List<TestcaseView> testcaseViews =
                testcases == null
                        ? List.of()
                        : testcases.stream().map(this::toTestcaseView).toList();
        return new TestcaseGroupView(
                group.getId(),
                group.getDatasetId(),
                group.getName(),
                group.getIsSample() != null && group.getIsSample() == 1,
                group.getWeight(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                testcaseViews);
    }

    private TestcaseView toTestcaseView(Testcase testcase) {
        return new TestcaseView(
                testcase.getId(),
                testcase.getGroupId(),
                testcase.getOrderIndex(),
                testcase.getInputFileId(),
                testcase.getOutputFileId(),
                testcase.getInputJson(),
                testcase.getOutputJson(),
                testcase.getOutputType(),
                testcase.getScore(),
                testcase.getCreatedAt(),
                testcase.getUpdatedAt());
    }
}
