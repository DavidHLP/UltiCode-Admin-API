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
import com.david.problem.dto.ProblemStatementPayload;
import com.david.problem.dto.ProblemStatementView;
import com.david.problem.dto.ProblemSummaryView;
import com.david.problem.dto.ProblemTagDto;
import com.david.problem.dto.ProblemUpsertRequest;
import com.david.problem.dto.TagOption;
import com.david.problem.entity.Category;
import com.david.problem.entity.Difficulty;
import com.david.problem.entity.Language;
import com.david.problem.entity.Problem;
import com.david.problem.entity.ProblemLanguageConfig;
import com.david.problem.entity.ProblemStatement;
import com.david.problem.entity.ProblemTag;
import com.david.problem.entity.Tag;
import com.david.problem.exception.BusinessException;
import com.david.problem.mapper.CategoryMapper;
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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProblemManagementService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE =
            new TypeReference<>() {};
    private static final List<String> SUPPORTED_PROBLEM_TYPES =
            List.of("coding", "sql", "shell", "concurrency", "interactive", "output-only");

    private final ProblemMapper problemMapper;
    private final ProblemStatementMapper problemStatementMapper;
    private final ProblemLanguageConfigMapper problemLanguageConfigMapper;
    private final ProblemTagMapper problemTagMapper;
    private final TagMapper tagMapper;
    private final DifficultyMapper difficultyMapper;
    private final CategoryMapper categoryMapper;
    private final LanguageMapper languageMapper;
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
            ObjectMapper objectMapper) {
        this.problemMapper = problemMapper;
        this.problemStatementMapper = problemStatementMapper;
        this.problemLanguageConfigMapper = problemLanguageConfigMapper;
        this.problemTagMapper = problemTagMapper;
        this.tagMapper = tagMapper;
        this.difficultyMapper = difficultyMapper;
        this.categoryMapper = categoryMapper;
        this.languageMapper = languageMapper;
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
            return new PageResult<>(List.of(), result.getTotal(), result.getCurrent(), result.getSize());
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
                    tagsGrouping.tagsByProblem()
                            .getOrDefault(problem.getId(), List.of())
                            .stream()
                            .map(tagsGrouping.tagsById()::get)
                            .filter(Objects::nonNull)
                            .map(tag -> new ProblemTagDto(tag.getId(), tag.getSlug(), tag.getName()))
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
                        : languageMapper.selectBatchIds(
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
                tagsGrouping.tagsByProblem()
                        .getOrDefault(problemId, List.of())
                        .stream()
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
        problem.setSlug(request.slug().trim());
        problem.setProblemType(request.problemType());
        problem.setDifficultyId(request.difficultyId());
        problem.setCategoryId(request.categoryId());
        problem.setCreatorId(request.creatorId());
        problem.setSolutionEntry(
                StringUtils.hasText(request.solutionEntry())
                        ? request.solutionEntry().trim()
                        : null);
        problem.setTimeLimitMs(request.timeLimitMs());
        problem.setMemoryLimitKb(request.memoryLimitKb());
        problem.setIsPublic(Boolean.FALSE.equals(request.isPublic()) ? 0 : 1);
        problem.setMetaJson(toMetaJson(request.meta()));
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

        existing.setSlug(request.slug().trim());
        existing.setProblemType(request.problemType());
        existing.setDifficultyId(request.difficultyId());
        existing.setCategoryId(request.categoryId());
        existing.setCreatorId(request.creatorId());
        existing.setSolutionEntry(
                StringUtils.hasText(request.solutionEntry())
                        ? request.solutionEntry().trim()
                        : null);
        existing.setTimeLimitMs(request.timeLimitMs());
        existing.setMemoryLimitKb(request.memoryLimitKb());
        existing.setIsPublic(Boolean.FALSE.equals(request.isPublic()) ? 0 : 1);
        existing.setMetaJson(toMetaJson(request.meta()));
        existing.setUpdatedAt(LocalDateTime.now());

        problemMapper.updateById(existing);

        replaceProblemStatements(problemId, request.statements());
        replaceProblemLanguageConfigs(problemId, request.languageConfigs());
        replaceProblemTags(problemId, request.tagIds());

        return getProblem(problemId, null);
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
                        .selectList(
                                Wrappers.lambdaQuery(Tag.class).orderByAsc(Tag::getName))
                        .stream()
                        .map(tag -> new TagOption(tag.getId(), tag.getSlug(), tag.getName()))
                        .toList();

        List<LanguageOption> languages =
                languageMapper
                        .selectList(Wrappers.lambdaQuery(Language.class).orderByAsc(Language::getId))
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
