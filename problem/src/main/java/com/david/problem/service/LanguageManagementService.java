package com.david.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.LanguageCreateRequest;
import com.david.problem.dto.LanguageUpdateRequest;
import com.david.problem.dto.LanguageView;
import com.david.problem.dto.PageResult;
import com.david.problem.entity.Language;
import com.david.problem.entity.ProblemLanguageConfig;
import com.david.problem.entity.ProblemStatement;
import com.david.problem.exception.BusinessException;
import com.david.problem.mapper.LanguageMapper;
import com.david.problem.mapper.ProblemLanguageConfigMapper;
import com.david.problem.mapper.ProblemStatementMapper;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LanguageManagementService {

    private final LanguageMapper languageMapper;
    private final ProblemLanguageConfigMapper problemLanguageConfigMapper;
    private final ProblemStatementMapper problemStatementMapper;

    public LanguageManagementService(
            LanguageMapper languageMapper,
            ProblemLanguageConfigMapper problemLanguageConfigMapper,
            ProblemStatementMapper problemStatementMapper) {
        this.languageMapper = languageMapper;
        this.problemLanguageConfigMapper = problemLanguageConfigMapper;
        this.problemStatementMapper = problemStatementMapper;
    }

    public PageResult<LanguageView> listLanguages(
            int page, int size, @Nullable String keyword, @Nullable Boolean isActive) {
        LambdaQueryWrapper<Language> query = Wrappers.lambdaQuery(Language.class);
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper ->
                            wrapper.like(Language::getCode, trimmed)
                                    .or()
                                    .like(Language::getDisplayName, trimmed));
        }
        if (isActive != null) {
            query.eq(Language::getIsActive, Boolean.TRUE.equals(isActive) ? 1 : 0);
        }
        query.orderByAsc(Language::getDisplayName);
        Page<Language> pager = new Page<>(page, size);
        Page<Language> result = languageMapper.selectPage(pager, query);
        List<Language> languages = result.getRecords();
        List<LanguageView> items =
                languages == null || languages.isEmpty()
                        ? List.of()
                        : languages.stream().map(this::toView).toList();
        return new PageResult<>(
                items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public LanguageView getLanguage(Integer languageId) {
        Language language = languageMapper.selectById(languageId);
        if (language == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "编程语言不存在");
        }
        return toView(language);
    }

    @Transactional
    public LanguageView createLanguage(LanguageCreateRequest request) {
        String code = normalizeCode(request.code());
        String displayName = normalizeDisplayName(request.displayName());
        String runtimeImage = normalizeRuntimeImage(request.runtimeImage());
        ensureCodeUnique(code, null);

        Language language = new Language();
        language.setCode(code);
        language.setDisplayName(displayName);
        language.setRuntimeImage(runtimeImage);
        language.setIsActive(Boolean.FALSE.equals(request.isActive()) ? 0 : 1);
        languageMapper.insert(language);
        return getLanguage(language.getId());
    }

    @Transactional
    public LanguageView updateLanguage(Integer languageId, LanguageUpdateRequest request) {
        Language existing = languageMapper.selectById(languageId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "编程语言不存在");
        }
        boolean changed = false;

        if (request.code() != null) {
            String code = normalizeCode(request.code());
            if (!Objects.equals(code, existing.getCode())) {
                ensureCodeUnique(code, languageId);
                updateProblemStatementsLanguageCode(existing.getCode(), code);
                existing.setCode(code);
                changed = true;
            }
        }
        if (request.displayName() != null) {
            String displayName = normalizeDisplayName(request.displayName());
            if (!Objects.equals(displayName, existing.getDisplayName())) {
                existing.setDisplayName(displayName);
                changed = true;
            }
        }
        if (request.runtimeImage() != null) {
            String runtimeImage = normalizeRuntimeImage(request.runtimeImage());
            if (!Objects.equals(runtimeImage, existing.getRuntimeImage())) {
                existing.setRuntimeImage(runtimeImage);
                changed = true;
            }
        }
        if (request.isActive() != null) {
            int active = Boolean.TRUE.equals(request.isActive()) ? 1 : 0;
            if (!Objects.equals(active, existing.getIsActive())) {
                existing.setIsActive(active);
                changed = true;
            }
        }
        if (!changed) {
            return toView(existing);
        }
        languageMapper.updateById(existing);
        return getLanguage(languageId);
    }

    @Transactional
    public void deleteLanguage(Integer languageId) {
        Language existing = languageMapper.selectById(languageId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "编程语言不存在");
        }
        Long configCount =
                problemLanguageConfigMapper.selectCount(
                        Wrappers.lambdaQuery(ProblemLanguageConfig.class)
                                .eq(ProblemLanguageConfig::getLanguageId, languageId));
        if (configCount != null && configCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "仍有题目使用该语言配置，无法删除");
        }
        Long statementCount =
                problemStatementMapper.selectCount(
                        Wrappers.lambdaQuery(ProblemStatement.class)
                                .eq(ProblemStatement::getLangCode, existing.getCode()));
        if (statementCount != null && statementCount > 0) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "仍有题面使用该语言代码，无法删除");
        }
        languageMapper.deleteById(languageId);
    }

    private void ensureCodeUnique(String code, @Nullable Integer excludeLanguageId) {
        LambdaQueryWrapper<Language> query =
                Wrappers.lambdaQuery(Language.class).eq(Language::getCode, code);
        if (excludeLanguageId != null) {
            query.ne(Language::getId, excludeLanguageId);
        }
        Long count = languageMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "语言编码已存在");
        }
    }

    private void updateProblemStatementsLanguageCode(String from, String to) {
        if (Objects.equals(from, to)) {
            return;
        }
        LambdaUpdateWrapper<ProblemStatement> update =
                Wrappers.lambdaUpdate(ProblemStatement.class).eq(ProblemStatement::getLangCode, from);
        update.set(ProblemStatement::getLangCode, to);
        problemStatementMapper.update(null, update);
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "语言编码不能为空");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        String trimmed = displayName.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "展示名称不能为空");
        }
        return trimmed;
    }

    private String normalizeRuntimeImage(@Nullable String runtimeImage) {
        if (!StringUtils.hasText(runtimeImage)) {
            return null;
        }
        return runtimeImage.trim();
    }

    private LanguageView toView(Language language) {
        boolean active = language.getIsActive() != null && language.getIsActive() == 1;
        return new LanguageView(
                language.getId(),
                language.getCode(),
                language.getDisplayName(),
                language.getRuntimeImage(),
                active);
    }
}
