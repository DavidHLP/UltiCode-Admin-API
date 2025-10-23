package com.david.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.DictionaryOption;
import com.david.problem.dto.LanguageOption;
import com.david.problem.dto.PageResult;
import com.david.problem.dto.ProblemDetailView;
import com.david.problem.dto.ProblemLanguageConfigPayload;
import com.david.problem.dto.ProblemLanguageConfigView;
import com.david.problem.dto.ProblemOptionsResponse;
import com.david.problem.dto.ProblemReviewDecisionRequest;
import com.david.problem.dto.ProblemStatementPayload;
import com.david.problem.dto.ProblemStatementView;
import com.david.problem.dto.ProblemSummaryView;
import com.david.problem.dto.ProblemTagDto;
import com.david.problem.dto.ProblemUpsertRequest;
import com.david.problem.dto.ProblemSubmitReviewRequest;
import com.david.problem.dto.TagOption;
import com.david.problem.entity.Category;
import com.david.problem.entity.Dataset;
import com.david.problem.entity.Difficulty;
import com.david.problem.entity.Language;
import com.david.problem.entity.Problem;
import com.david.problem.entity.ProblemLanguageConfig;
import com.david.problem.entity.ProblemStatement;
import com.david.problem.entity.ProblemTag;
import com.david.problem.entity.Tag;
import com.david.problem.exception.BusinessException;
import com.david.problem.mapper.CategoryMapper;
import com.david.problem.mapper.DatasetMapper;
import com.david.problem.mapper.DifficultyMapper;
import com.david.problem.mapper.LanguageMapper;
import com.david.problem.mapper.ProblemLanguageConfigMapper;
import com.david.problem.mapper.ProblemMapper;
import com.david.problem.mapper.ProblemStatementMapper;
import com.david.problem.mapper.ProblemTagMapper;
import com.david.problem.mapper.TagMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class ProblemManagementService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final List<String> SUPPORTED_PROBLEM_TYPES =
            List.of("coding", "sql", "shell", "concurrency", "interactive", "output-only");

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_IN_REVIEW = "in_review";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_READY = "ready";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ARCHIVED = "archived";

    private static final String REVIEW_PENDING = "pending";
    private static final String REVIEW_APPROVED = "approved";
    private static final String REVIEW_REJECTED = "rejected";

    private static final Set<String> LIFECYCLE_STATUSES =
            Set.of(
                    STATUS_DRAFT,
                    STATUS_IN_REVIEW,
                    STATUS_APPROVED,
                    STATUS_READY,
                    STATUS_PUBLISHED,
                    STATUS_ARCHIVED);
    private static final Set<String> REVIEW_STATUSES =
            Set.of(REVIEW_PENDING, REVIEW_APPROVED, REVIEW_REJECTED);

    private final ProblemMapper problemMapper;
    private final ProblemStatementMapper problemStatementMapper;
    private final ProblemLanguageConfigMapper problemLanguageConfigMapper;
    private final ProblemTagMapper problemTagMapper;
    private final TagMapper tagMapper;
    private final DifficultyMapper difficultyMapper;
    private final CategoryMapper categoryMapper;
    private final LanguageMapper languageMapper;
    private final DatasetMapper datasetMapper;
    private final ObjectMapper objectMapper;

    public ProblemManagementService(
            ProblemMapper problemMapper,
            ProblemStatementMapper problemStatementMapper,
            ProblemLanguageConfigMapper problemLanguageConfigMapper,
            ProblemTagMapper problemTagMapper,
            TagMapper tagMapper,
            DifficultyMapper difficultyMapper,
            CategoryMapper categoryMapper,
            LanguageMapper languageMapper,
            DatasetMapper datasetMapper,
            ObjectMapper objectMapper) {
        this.problemMapper = problemMapper;
        this.problemStatementMapper = problemStatementMapper;
        this.problemLanguageConfigMapper = problemLanguageConfigMapper;
        this.problemTagMapper = problemTagMapper;
        this.tagMapper = tagMapper;
        this.difficultyMapper = difficultyMapper;
        this.categoryMapper = categoryMapper;
        this.languageMapper = languageMapper;
        this.datasetMapper = datasetMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<ProblemSummaryView> listProblems(
            int page,
            int size,
            @Nullable String keyword,
            @Nullable String problemType,
            @Nullable Integer difficultyId,
            @Nullable Integer categoryId,
            @Nullable Boolean isPublic,
            @Nullable String lifecycleStatus,
            @Nullable String reviewStatus,
            @Nullable String preferredLangCode) {
        Page<Problem> pager = new Page<>(page, size);
        LambdaQueryWrapper<Problem> query = Wrappers.lambdaQuery(Problem.class);

        if (StringUtils.hasText(problemType)) {
            ensureValidProblemType(problemType);
            query.eq(Problem::getProblemType, problemType);
        }
        if (difficultyId != null) {
            query.eq(Problem::getDifficultyId, difficultyId);
        }
        if (categoryId != null) {
            query.eq(Problem::getCategoryId, categoryId);
        }
        if (isPublic != null) {
            query.eq(Problem::getIsPublic, isPublic ? 1 : 0);
        }
        if (StringUtils.hasText(lifecycleStatus)) {
            query.eq(Problem::getLifecycleStatus, normalizeLifecycleStatus(lifecycleStatus));
        }
        if (StringUtils.hasText(reviewStatus)) {
            query.eq(Problem::getReviewStatus, normalizeReviewStatus(reviewStatus));
        }
        if (StringUtils.hasText(keyword)) {
            List<Long> matchedIds = findProblemIdsByStatementKeyword(keyword, preferredLangCode);
            query.and(
                    wrapper -> {
                        wrapper.like(Problem::getSlug, keyword);
                        if (!matchedIds.isEmpty()) {
                            wrapper.or().in(Problem::getId, matchedIds);
                        }
                    });
        }
        query.orderByDesc(Problem::getUpdatedAt);

        Page<Problem> result = problemMapper.selectPage(pager, query);
        List<Problem> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(
                    List.of(), result.getTotal(), result.getCurrent(), result.getSize());
        }

        List<Long> problemIds = records.stream().map(Problem::getId).toList();
        Map<Long, ProblemStatement> statementMap =
                loadPreferredStatements(problemIds, preferredLangCode);
        Map<Integer, Difficulty> difficultyMap =
                loadDifficulties(
                        records.stream()
                                .map(Problem::getDifficultyId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
        Map<Integer, Category> categoryMap =
                loadCategories(
                        records.stream()
                                .map(Problem::getCategoryId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
        TagsGrouping tagsGrouping = loadTags(problemIds);

        List<ProblemSummaryView> items = new ArrayList<>(records.size());
        for (Problem problem : records) {
            ProblemStatement statement = statementMap.get(problem.getId());
            Difficulty difficulty = difficultyMap.get(problem.getDifficultyId());
            Category category = categoryMap.get(problem.getCategoryId());
            List<ProblemTagDto> tagDtos =
                    tagsGrouping.tagsByProblem().getOrDefault(problem.getId(), List.of()).stream()
                            .map(tagsGrouping.tagsById()::get)
                            .filter(Objects::nonNull)
                            .map(
                                    tag ->
                                            new ProblemTagDto(
                                                    tag.getId(), tag.getSlug(), tag.getName()))
                            .toList();

            items.add(
                    new ProblemSummaryView(
                            problem.getId(),
                            problem.getSlug(),
                            statement != null ? statement.getTitle() : null,
                            problem.getProblemType(),
                            problem.getDifficultyId(),
                            difficulty != null ? difficulty.getCode() : null,
                            problem.getCategoryId(),
                            category != null ? category.getName() : null,
                            problem.getIsPublic() != null && problem.getIsPublic() == 1,
                            problem.getLifecycleStatus(),
                            problem.getReviewStatus(),
                            problem.getActiveDatasetId(),
                            problem.getReviewedBy(),
                            problem.getReviewedAt(),
                            problem.getSubmittedForReviewAt(),
                            problem.getTimeLimitMs(),
                            problem.getMemoryLimitKb(),
                            problem.getUpdatedAt(),
                            tagDtos,
                            parseMeta(problem.getMetaJson())));
        }

        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public ProblemDetailView getProblem(Long problemId, @Nullable String preferredLangCode) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }

        Difficulty difficulty = null;
        if (problem.getDifficultyId() != null) {
            difficulty = difficultyMapper.selectById(problem.getDifficultyId());
        }
        Category category = null;
        if (problem.getCategoryId() != null) {
            category = categoryMapper.selectById(problem.getCategoryId());
        }

        List<ProblemStatement> statements =
                problemStatementMapper.selectList(
                        Wrappers.lambdaQuery(ProblemStatement.class)
                                .eq(ProblemStatement::getProblemId, problemId));
        List<ProblemStatementView> statementViews =
                statements.stream()
                        .map(
                                s ->
                                        new ProblemStatementView(
                                                s.getId(),
                                                s.getLangCode(),
                                                s.getTitle(),
                                                s.getDescriptionMd(),
                                                s.getConstraintsMd(),
                                                s.getExamplesMd()))
                        .toList();

        List<ProblemLanguageConfig> configs =
                problemLanguageConfigMapper.selectList(
                        Wrappers.lambdaQuery(ProblemLanguageConfig.class)
                                .eq(ProblemLanguageConfig::getProblemId, problemId));
        Map<Integer, Language> languages =
                configs.isEmpty()
                        ? Map.of()
                        : languageMapper
                                .selectBatchIds(
                                        configs.stream()
                                                .map(ProblemLanguageConfig::getLanguageId)
                                                .collect(Collectors.toSet()))
                                .stream()
                                .collect(Collectors.toMap(Language::getId, l -> l));
        List<ProblemLanguageConfigView> languageViews =
                configs.stream()
                        .map(
                                cfg -> {
                                    Language lang = languages.get(cfg.getLanguageId());
                                    return new ProblemLanguageConfigView(
                                            cfg.getId(),
                                            cfg.getLanguageId(),
                                            lang != null ? lang.getCode() : null,
                                            lang != null ? lang.getDisplayName() : null,
                                            cfg.getFunctionName(),
                                            cfg.getStarterCode());
                                })
                        .toList();

        TagsGrouping tagsGrouping = loadTags(List.of(problemId));
        List<ProblemTagDto> tagDtos =
                tagsGrouping.tagsByProblem().getOrDefault(problemId, List.of()).stream()
                        .map(tagsGrouping.tagsById()::get)
                        .filter(Objects::nonNull)
                        .map(tag -> new ProblemTagDto(tag.getId(), tag.getSlug(), tag.getName()))
                        .toList();

        return new ProblemDetailView(
                problem.getId(),
                problem.getSlug(),
                problem.getProblemType(),
                problem.getDifficultyId(),
                difficulty != null ? difficulty.getCode() : null,
                problem.getCategoryId(),
                category != null ? category.getName() : null,
                problem.getCreatorId(),
                problem.getSolutionEntry(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitKb(),
                problem.getIsPublic() != null && problem.getIsPublic() == 1,
                problem.getLifecycleStatus(),
                problem.getReviewStatus(),
                problem.getReviewedBy(),
                problem.getReviewedAt(),
                problem.getReviewNotes(),
                problem.getSubmittedForReviewAt(),
                problem.getActiveDatasetId(),
                parseMeta(problem.getMetaJson()),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                statementViews,
                languageViews,
                tagDtos);
    }

    @Transactional
    public ProblemDetailView createProblem(ProblemUpsertRequest request) {
        ensureValidProblemType(request.problemType());
        ensureDifficultyExists(request.difficultyId());
        ensureCategoryExists(request.categoryId());
        ensureSlugUnique(request.slug(), null);
        ensureStatementsValid(request.statements());
        ensureLanguagesExist(request.languageConfigs());
        ensureTagsExist(request.tagIds());

        Problem problem = new Problem();
        problem.setLifecycleStatus(STATUS_DRAFT);
        problem.setReviewStatus(REVIEW_PENDING);
        problem.setReviewNotes(null);
        problem.setReviewedAt(null);
        problem.setReviewedBy(null);
        problem.setSubmittedForReviewAt(null);
        problem.setIsPublic(0);
        setProblem(request, problem, true);
        problem.setCreatedAt(LocalDateTime.now());
        problem.setUpdatedAt(LocalDateTime.now());

        problemMapper.insert(problem);

        replaceProblemStatements(problem.getId(), request.statements());
        replaceProblemLanguageConfigs(problem.getId(), request.languageConfigs());
        replaceProblemTags(problem.getId(), request.tagIds());

        return getProblem(problem.getId(), null);
    }

    @Transactional
    public ProblemDetailView updateProblem(Long problemId, ProblemUpsertRequest request) {
        Problem existing = problemMapper.selectById(problemId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }

        ensureValidProblemType(request.problemType());
        ensureDifficultyExists(request.difficultyId());
        ensureCategoryExists(request.categoryId());
        ensureSlugUnique(request.slug(), problemId);
        ensureStatementsValid(request.statements());
        ensureLanguagesExist(request.languageConfigs());
        ensureTagsExist(request.tagIds());

        setProblem(request, existing, false);
        existing.setUpdatedAt(LocalDateTime.now());

        problemMapper.updateById(existing);

        replaceProblemStatements(problemId, request.statements());
        replaceProblemLanguageConfigs(problemId, request.languageConfigs());
        replaceProblemTags(problemId, request.tagIds());

        return getProblem(problemId, null);
    }

    @Transactional
    public ProblemDetailView submitForReview(
            Long problemId, @Nullable ProblemSubmitReviewRequest request) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        if (STATUS_ARCHIVED.equals(problem.getLifecycleStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "已归档的题目无法提交审核");
        }
        if (STATUS_IN_REVIEW.equals(problem.getLifecycleStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目已处于审核流程中");
        }

        problem.setLifecycleStatus(STATUS_IN_REVIEW);
        problem.setReviewStatus(REVIEW_PENDING);
        problem.setReviewedBy(null);
        problem.setReviewedAt(null);
        problem.setSubmittedForReviewAt(LocalDateTime.now());
        if (problem.getIsPublic() != null && problem.getIsPublic() == 1) {
            problem.setIsPublic(0);
        }
        if (request != null && StringUtils.hasText(request.notes())) {
            problem.setReviewNotes(request.notes().trim());
        }
        problem.setUpdatedAt(LocalDateTime.now());
        problemMapper.updateById(problem);

        return getProblem(problemId, null);
    }

    @Transactional
    public ProblemDetailView reviewProblem(
            Long problemId, ProblemReviewDecisionRequest request) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        if (!STATUS_IN_REVIEW.equals(problem.getLifecycleStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目当前状态不支持审核操作");
        }

        LocalDateTime now = LocalDateTime.now();
        problem.setReviewedBy(request.reviewerId());
        problem.setReviewedAt(now);
        if (StringUtils.hasText(request.notes())) {
            problem.setReviewNotes(request.notes().trim());
        }
        if (Boolean.TRUE.equals(request.approved())) {
            problem.setReviewStatus(REVIEW_APPROVED);
            problem.setLifecycleStatus(STATUS_APPROVED);
        } else {
            problem.setReviewStatus(REVIEW_REJECTED);
            problem.setLifecycleStatus(STATUS_DRAFT);
            problem.setIsPublic(0);
        }
        problem.setUpdatedAt(now);
        problemMapper.updateById(problem);
        return getProblem(problemId, null);
    }

    @Transactional
    public ProblemDetailView togglePublish(
            Long problemId, boolean publish, @Nullable String notes) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        if (publish) {
            ensurePublicationPrerequisites(problem);
            problem.setIsPublic(1);
            problem.setLifecycleStatus(STATUS_PUBLISHED);
        } else {
            problem.setIsPublic(0);
            if (STATUS_PUBLISHED.equals(problem.getLifecycleStatus())) {
                if (problem.getActiveDatasetId() != null) {
                    problem.setLifecycleStatus(STATUS_READY);
                } else if (REVIEW_APPROVED.equals(problem.getReviewStatus())) {
                    problem.setLifecycleStatus(STATUS_APPROVED);
                } else if (!STATUS_ARCHIVED.equals(problem.getLifecycleStatus())) {
                    problem.setLifecycleStatus(STATUS_DRAFT);
                }
            }
        }
        if (StringUtils.hasText(notes)) {
            problem.setReviewNotes(notes.trim());
        }
        problem.setUpdatedAt(LocalDateTime.now());
        problemMapper.updateById(problem);
        return getProblem(problemId, null);
    }

    private void setProblem(ProblemUpsertRequest request, Problem target, boolean isNew) {
        target.setSlug(request.slug().trim());
        target.setProblemType(request.problemType());
        target.setDifficultyId(request.difficultyId());
        target.setCategoryId(request.categoryId());
        target.setCreatorId(request.creatorId());
        target.setSolutionEntry(
                StringUtils.hasText(request.solutionEntry())
                        ? request.solutionEntry().trim()
                        : null);
        target.setTimeLimitMs(request.timeLimitMs());
        target.setMemoryLimitKb(request.memoryLimitKb());
        target.setMetaJson(toMetaJson(request.meta()));
        applyPublicationFlag(target, request.isPublic(), isNew);
    }

    public ProblemOptionsResponse loadOptions() {
        List<DictionaryOption> difficulties =
                difficultyMapper
                        .selectList(
                                Wrappers.lambdaQuery(Difficulty.class)
                                        .orderByAsc(Difficulty::getSortKey))
                        .stream()
                        .map(d -> new DictionaryOption(d.getId(), d.getCode(), d.getCode()))
                        .toList();

        List<DictionaryOption> categories =
                categoryMapper
                        .selectList(
                                Wrappers.lambdaQuery(Category.class).orderByAsc(Category::getName))
                        .stream()
                        .map(c -> new DictionaryOption(c.getId(), c.getCode(), c.getName()))
                        .toList();

        List<TagOption> tags =
                tagMapper
                        .selectList(Wrappers.lambdaQuery(Tag.class).orderByAsc(Tag::getName))
                        .stream()
                        .map(tag -> new TagOption(tag.getId(), tag.getSlug(), tag.getName()))
                        .toList();

        List<LanguageOption> languages =
                languageMapper
                        .selectList(
                                Wrappers.lambdaQuery(Language.class).orderByAsc(Language::getId))
                        .stream()
                        .map(
                                lang ->
                                        new LanguageOption(
                                                lang.getId(),
                                                lang.getCode(),
                                                lang.getDisplayName(),
                                                lang.getIsActive() == null
                                                        ? null
                                                        : lang.getIsActive() == 1))
                        .toList();

        return new ProblemOptionsResponse(
                difficulties, categories, tags, languages, SUPPORTED_PROBLEM_TYPES);
    }

    private void applyPublicationFlag(
            Problem problem, @Nullable Boolean requested, boolean isNew) {
        if (requested == null) {
            if (isNew && problem.getIsPublic() == null) {
                problem.setIsPublic(0);
            }
            return;
        }
        if (Boolean.TRUE.equals(requested)) {
            ensurePublicationPrerequisites(problem);
            problem.setIsPublic(1);
            problem.setLifecycleStatus(STATUS_PUBLISHED);
        } else {
            problem.setIsPublic(0);
            if (!isNew && STATUS_PUBLISHED.equals(problem.getLifecycleStatus())) {
                if (problem.getActiveDatasetId() != null) {
                    problem.setLifecycleStatus(STATUS_READY);
                } else if (REVIEW_APPROVED.equals(problem.getReviewStatus())) {
                    problem.setLifecycleStatus(STATUS_APPROVED);
                } else if (!STATUS_ARCHIVED.equals(problem.getLifecycleStatus())) {
                    problem.setLifecycleStatus(STATUS_DRAFT);
                }
            } else if (isNew && problem.getLifecycleStatus() == null) {
                problem.setLifecycleStatus(STATUS_DRAFT);
            }
        }
    }

    private void ensurePublicationPrerequisites(Problem problem) {
        if (!REVIEW_APPROVED.equals(problem.getReviewStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目尚未通过审核，无法公布");
        }
        Long datasetId = problem.getActiveDatasetId();
        if (datasetId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先为题目激活数据集");
        }
        Dataset dataset = datasetMapper.selectById(datasetId);
        if (dataset == null || dataset.getIsActive() == null || dataset.getIsActive() != 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "激活数据集不存在或未启用，无法公布");
        }
        if (STATUS_ARCHIVED.equals(problem.getLifecycleStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "已归档题目无法公布");
        }
        if (STATUS_IN_REVIEW.equals(problem.getLifecycleStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "审核中的题目无法公布");
        }
        if (!STATUS_PUBLISHED.equals(problem.getLifecycleStatus())) {
            problem.setLifecycleStatus(STATUS_READY);
        }
    }

    private String normalizeLifecycleStatus(String lifecycleStatus) {
        String normalized = lifecycleStatus.trim().toLowerCase(Locale.ROOT);
        if (!LIFECYCLE_STATUSES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的生命周期状态");
        }
        return normalized;
    }

    private String normalizeReviewStatus(String reviewStatus) {
        String normalized = reviewStatus.trim().toLowerCase(Locale.ROOT);
        if (!REVIEW_STATUSES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的审核状态");
        }
        return normalized;
    }

    private void ensureValidProblemType(String problemType) {
        if (!SUPPORTED_PROBLEM_TYPES.contains(problemType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的题目类型");
        }
    }

    private void ensureDifficultyExists(Integer difficultyId) {
        if (difficultyId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "难度不能为空");
        }
        if (difficultyMapper.selectById(difficultyId) == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "指定的难度不存在");
        }
    }

    private void ensureCategoryExists(@Nullable Integer categoryId) {
        if (categoryId == null) {
            return;
        }
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "指定的分类不存在");
        }
    }

    private void ensureSlugUnique(String slug, @Nullable Long excludeProblemId) {
        if (!StringUtils.hasText(slug)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题目别名不能为空");
        }
        LambdaQueryWrapper<Problem> query =
                Wrappers.lambdaQuery(Problem.class).eq(Problem::getSlug, slug.trim());
        if (excludeProblemId != null) {
            query.ne(Problem::getId, excludeProblemId);
        }
        Long count = problemMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "题目别名已存在");
        }
    }

    private void ensureStatementsValid(List<ProblemStatementPayload> statements) {
        if (CollectionUtils.isEmpty(statements)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "题面不能为空");
        }
        Set<String> langCodes = new LinkedHashSet<>();
        for (ProblemStatementPayload statement : statements) {
            if (!StringUtils.hasText(statement.langCode())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "题面语言代码不能为空");
            }
            if (!langCodes.add(statement.langCode())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "同一语言的题面重复");
            }
            if (!StringUtils.hasText(statement.title())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "题面标题不能为空");
            }
            if (!StringUtils.hasText(statement.descriptionMd())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "题面描述不能为空");
            }
        }
    }

    private void ensureLanguagesExist(@Nullable List<ProblemLanguageConfigPayload> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        Set<Integer> languageIds =
                configs.stream()
                        .map(ProblemLanguageConfigPayload::languageId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        if (languageIds.isEmpty()) {
            return;
        }
        List<Language> languages = languageMapper.selectBatchIds(languageIds);
        if (languages.size() != languageIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效的语言配置");
        }
    }

    private void ensureTagsExist(@Nullable List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(tagIds);
        List<Tag> tags = tagMapper.selectBatchIds(uniqueIds);
        if (tags.size() != uniqueIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效的标签");
        }
    }

    private Map<Long, ProblemStatement> loadPreferredStatements(
            List<Long> problemIds, @Nullable String preferredLangCode) {
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        List<ProblemStatement> statements =
                problemStatementMapper.selectList(
                        Wrappers.lambdaQuery(ProblemStatement.class)
                                .in(ProblemStatement::getProblemId, problemIds));
        if (statements.isEmpty()) {
            return Map.of();
        }
        Map<Long, ProblemStatement> preferred = new HashMap<>();
        Map<Long, ProblemStatement> anyStatement = new HashMap<>();
        for (ProblemStatement statement : statements) {
            anyStatement.putIfAbsent(statement.getProblemId(), statement);
            if (preferredLangCode != null
                    && preferredLangCode.equalsIgnoreCase(statement.getLangCode())) {
                preferred.put(statement.getProblemId(), statement);
            }
        }
        anyStatement.forEach(preferred::putIfAbsent);
        return preferred;
    }

    private Map<Integer, Difficulty> loadDifficulties(Set<Integer> difficultyIds) {
        if (difficultyIds.isEmpty()) {
            return Map.of();
        }
        return difficultyMapper.selectBatchIds(difficultyIds).stream()
                .collect(Collectors.toMap(Difficulty::getId, d -> d));
    }

    private Map<Integer, Category> loadCategories(Set<Integer> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }

    private TagsGrouping loadTags(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return new TagsGrouping(Map.of(), Map.of());
        }
        List<ProblemTag> relations =
                problemTagMapper.selectList(
                        Wrappers.lambdaQuery(ProblemTag.class)
                                .in(ProblemTag::getProblemId, problemIds));
        if (relations.isEmpty()) {
            return new TagsGrouping(Map.of(), Map.of());
        }
        Map<Long, List<Long>> tagIdsByProblem = new HashMap<>();
        Set<Long> tagIds = new LinkedHashSet<>();
        for (ProblemTag relation : relations) {
            tagIdsByProblem
                    .computeIfAbsent(relation.getProblemId(), key -> new ArrayList<>())
                    .add(relation.getTagId());
            tagIds.add(relation.getTagId());
        }
        Map<Long, Tag> tagsById =
                tagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, tag -> tag));
        return new TagsGrouping(tagsById, tagIdsByProblem);
    }

    private List<Long> findProblemIdsByStatementKeyword(
            String keyword, @Nullable String preferredLangCode) {
        LambdaQueryWrapper<ProblemStatement> query = Wrappers.lambdaQuery(ProblemStatement.class);
        if (preferredLangCode != null) {
            query.eq(ProblemStatement::getLangCode, preferredLangCode);
        }
        query.and(
                wrapper ->
                        wrapper.like(ProblemStatement::getTitle, keyword)
                                .or()
                                .like(ProblemStatement::getDescriptionMd, keyword));
        return problemStatementMapper.selectList(query).stream()
                .map(ProblemStatement::getProblemId)
                .toList();
    }

    private void replaceProblemStatements(
            Long problemId, List<ProblemStatementPayload> statements) {
        problemStatementMapper.delete(
                Wrappers.lambdaQuery(ProblemStatement.class)
                        .eq(ProblemStatement::getProblemId, problemId));
        for (ProblemStatementPayload payload : statements) {
            ProblemStatement statement = new ProblemStatement();
            statement.setProblemId(problemId);
            statement.setLangCode(payload.langCode());
            statement.setTitle(payload.title());
            statement.setDescriptionMd(payload.descriptionMd());
            statement.setConstraintsMd(payload.constraintsMd());
            statement.setExamplesMd(payload.examplesMd());
            statement.setCreatedAt(LocalDateTime.now());
            statement.setUpdatedAt(LocalDateTime.now());
            problemStatementMapper.insert(statement);
        }
    }

    private void replaceProblemLanguageConfigs(
            Long problemId, @Nullable List<ProblemLanguageConfigPayload> configs) {
        problemLanguageConfigMapper.delete(
                Wrappers.lambdaQuery(ProblemLanguageConfig.class)
                        .eq(ProblemLanguageConfig::getProblemId, problemId));
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (ProblemLanguageConfigPayload payload : configs) {
            ProblemLanguageConfig config = new ProblemLanguageConfig();
            config.setProblemId(problemId);
            config.setLanguageId(payload.languageId());
            config.setFunctionName(
                    StringUtils.hasText(payload.functionName())
                            ? payload.functionName().trim()
                            : null);
            config.setStarterCode(payload.starterCode());
            problemLanguageConfigMapper.insert(config);
        }
    }

    private void replaceProblemTags(Long problemId, @Nullable List<Long> tagIds) {
        problemTagMapper.delete(
                Wrappers.lambdaQuery(ProblemTag.class).eq(ProblemTag::getProblemId, problemId));
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        for (Long tagId : new LinkedHashSet<>(tagIds)) {
            ProblemTag relation = new ProblemTag();
            relation.setProblemId(problemId);
            relation.setTagId(tagId);
            problemTagMapper.insert(relation);
        }
    }

    private Map<String, Object> parseMeta(@Nullable String metaJson) {
        if (!StringUtils.hasText(metaJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metaJson, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "元数据解析失败");
        }
    }

    private String toMetaJson(@Nullable Map<String, Object> meta) {
        if (meta == null || meta.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(new LinkedHashMap<>(meta));
        } catch (JsonProcessingException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "元数据序列化失败");
        }
    }

    private record TagsGrouping(Map<Long, Tag> tagsById, Map<Long, List<Long>> tagsByProblem) {}
}
