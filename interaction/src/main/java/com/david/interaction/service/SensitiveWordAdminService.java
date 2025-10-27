package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.core.exception.BusinessException;
import com.david.interaction.dto.PageResult;
import com.david.interaction.dto.SensitiveWordQuery;
import com.david.interaction.dto.SensitiveWordUpsertRequest;
import com.david.interaction.dto.SensitiveWordView;
import com.david.interaction.entity.SensitiveWord;
import com.david.interaction.mapper.SensitiveWordMapper;
import com.david.interaction.service.model.SensitiveWordAnalysisResult;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SensitiveWordAdminService {

    private static final long ACTIVE_WORD_CACHE_TTL_MILLIS = 60_000L;

    private final SensitiveWordMapper sensitiveWordMapper;
    private final Object cacheLock = new Object();
    private volatile List<SensitiveWord> cachedActiveWords = List.of();
    private volatile long activeWordCacheLoadedAt = 0L;

    public SensitiveWordAdminService(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    public PageResult<SensitiveWordView> listSensitiveWords(SensitiveWordQuery query) {
        int page = query.page() == null || query.page() < 1 ? 1 : query.page();
        int size = query.size() == null || query.size() < 1 ? 10 : query.size();
        Page<SensitiveWord> pager = new Page<>(page, size);

        LambdaQueryWrapper<SensitiveWord> wrapper = Wrappers.lambdaQuery(SensitiveWord.class);
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            wrapper.and(
                    w ->
                            w.like(SensitiveWord::getWord, keyword)
                                    .or()
                                    .like(SensitiveWord::getDescription, keyword));
        }
        if (StringUtils.hasText(query.category())) {
            wrapper.eq(SensitiveWord::getCategory, query.category().trim());
        }
        if (StringUtils.hasText(query.level())) {
            wrapper.eq(SensitiveWord::getLevel, query.level().trim());
        }
        if (query.active() != null) {
            wrapper.eq(SensitiveWord::getActive, query.active());
        }
        wrapper.orderByDesc(SensitiveWord::getUpdatedAt);

        Page<SensitiveWord> result = sensitiveWordMapper.selectPage(pager, wrapper);
        List<SensitiveWordView> items = result.getRecords().stream().map(this::toView).toList();

        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public SensitiveWordView createSensitiveWord(SensitiveWordUpsertRequest request) {
        ensureWordUnique(request.word(), null);

        SensitiveWord entity = new SensitiveWord();
        setSensitiveWord(request, entity);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        sensitiveWordMapper.insert(entity);
        invalidateActiveWordCache();
        return toView(entity);
    }

    private void setSensitiveWord(SensitiveWordUpsertRequest request, SensitiveWord entity) {
        entity.setWord(request.word().trim());
        entity.setCategory(
                StringUtils.hasText(request.category()) ? request.category().trim() : null);
        entity.setLevel(request.level().trim().toLowerCase(Locale.ROOT));
        entity.setReplacement(
                StringUtils.hasText(request.replacement()) ? request.replacement().trim() : null);
        entity.setDescription(
                StringUtils.hasText(request.description()) ? request.description().trim() : null);
        entity.setActive(request.active() == null ? Boolean.TRUE : request.active());
    }

    @Transactional
    public SensitiveWordView updateSensitiveWord(Long id, SensitiveWordUpsertRequest request) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "敏感词不存在");
        }
        ensureWordUnique(request.word(), id);

        setSensitiveWord(request, existing);
        existing.setUpdatedAt(LocalDateTime.now());

        sensitiveWordMapper.updateById(existing);
        invalidateActiveWordCache();
        return toView(existing);
    }

    @Transactional
    public void deleteSensitiveWord(Long id) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            return;
        }
        sensitiveWordMapper.deleteById(id);
        invalidateActiveWordCache();
    }

    public SensitiveWordAnalysisResult analyzeContent(String content) {
        if (!StringUtils.hasText(content)) {
            return SensitiveWordAnalysisResult.empty();
        }
        return analyzeContent(content, loadActiveWords());
    }

    public List<SensitiveWord> loadActiveWords() {
        long now = System.currentTimeMillis();
        if (now - activeWordCacheLoadedAt < ACTIVE_WORD_CACHE_TTL_MILLIS) {
            return cachedActiveWords;
        }
        synchronized (cacheLock) {
            if (now - activeWordCacheLoadedAt < ACTIVE_WORD_CACHE_TTL_MILLIS) {
                return cachedActiveWords;
            }
            List<SensitiveWord> latest =
                    sensitiveWordMapper.selectList(
                            Wrappers.lambdaQuery(SensitiveWord.class)
                                    .eq(SensitiveWord::getActive, Boolean.TRUE));
            cachedActiveWords =
                    latest == null || latest.isEmpty() ? List.of() : List.copyOf(latest);
            activeWordCacheLoadedAt = now;
            return cachedActiveWords;
        }
    }

    public SensitiveWordAnalysisResult analyzeContent(
            String content, List<SensitiveWord> activeWords) {
        if (!StringUtils.hasText(content)) {
            return SensitiveWordAnalysisResult.empty();
        }
        if (activeWords == null || activeWords.isEmpty()) {
            return SensitiveWordAnalysisResult.empty();
        }
        String normalizedContent = content.toLowerCase(Locale.ROOT);
        List<SensitiveWord> matched =
                activeWords.stream()
                        .filter(word -> containsWord(normalizedContent, word.getWord()))
                        .toList();
        if (matched.isEmpty()) {
            return SensitiveWordAnalysisResult.empty();
        }

        Map<String, SensitiveWord> wordIndex =
                matched.stream()
                        .collect(
                                Collectors.toMap(
                                        SensitiveWord::getWord, Function.identity(), (a, b) -> a));
        List<String> hits = new ArrayList<>(wordIndex.keySet());
        boolean blocked =
                matched.stream().anyMatch(word -> Objects.equals(word.getLevel(), "block"));
        boolean needReview =
                matched.stream().anyMatch(word -> Objects.equals(word.getLevel(), "review"));
        String riskLevel;
        if (blocked) {
            riskLevel = "high";
        } else if (needReview) {
            riskLevel = "medium";
        } else {
            riskLevel = "low";
        }
        return new SensitiveWordAnalysisResult(true, blocked, needReview, riskLevel, hits);
    }

    private boolean containsWord(String normalizedContent, String word) {
        if (!StringUtils.hasText(word)) {
            return false;
        }
        String target = word.toLowerCase(Locale.ROOT);
        return normalizedContent.contains(target);
    }

    private void ensureWordUnique(String word, Long excludeId) {
        if (!StringUtils.hasText(word)) {
            return;
        }
        LambdaQueryWrapper<SensitiveWord> wrapper =
                Wrappers.lambdaQuery(SensitiveWord.class).eq(SensitiveWord::getWord, word.trim());
        if (excludeId != null) {
            wrapper.ne(SensitiveWord::getId, excludeId);
        }
        Long count = sensitiveWordMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "敏感词已存在");
        }
    }

    private SensitiveWordView toView(SensitiveWord entity) {
        return new SensitiveWordView(
                entity.getId(),
                entity.getWord(),
                entity.getCategory(),
                entity.getLevel(),
                entity.getReplacement(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private void invalidateActiveWordCache() {
        activeWordCacheLoadedAt = 0L;
        cachedActiveWords = List.of();
    }
}
