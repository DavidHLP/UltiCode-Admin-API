package com.david.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.interaction.dto.BookmarkDeleteRequest;
import com.david.interaction.dto.BookmarkQuery;
import com.david.interaction.dto.BookmarkView;
import com.david.interaction.dto.PageResult;
import com.david.interaction.entity.Bookmark;
import com.david.interaction.entity.SensitiveWord;
import com.david.interaction.mapper.BookmarkMapper;
import com.david.interaction.service.model.SensitiveWordAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class BookmarkAdminService {

    private final BookmarkMapper bookmarkMapper;
    private final ObjectMapper objectMapper;
    private final SensitiveWordAdminService sensitiveWordAdminService;

    public BookmarkAdminService(
            BookmarkMapper bookmarkMapper,
            ObjectMapper objectMapper,
            SensitiveWordAdminService sensitiveWordAdminService) {
        this.bookmarkMapper = bookmarkMapper;
        this.objectMapper = objectMapper;
        this.sensitiveWordAdminService = sensitiveWordAdminService;
    }

    public PageResult<BookmarkView> listBookmarks(BookmarkQuery query) {
        int page = query.page() == null || query.page() < 1 ? 1 : query.page();
        int size = query.size() == null || query.size() < 1 ? 10 : query.size();
        Page<Bookmark> pager = new Page<>(page, size);

        LambdaQueryWrapper<Bookmark> wrapper = Wrappers.lambdaQuery(Bookmark.class);
        if (query.userId() != null) {
            wrapper.eq(Bookmark::getUserId, query.userId());
        }
        if (StringUtils.hasText(query.entityType())) {
            wrapper.eq(Bookmark::getEntityType, query.entityType().trim());
        }
        if (query.entityId() != null) {
            wrapper.eq(Bookmark::getEntityId, query.entityId());
        }
        if (StringUtils.hasText(query.visibility())) {
            wrapper.eq(Bookmark::getVisibility, query.visibility().trim());
        }
        if (StringUtils.hasText(query.source())) {
            wrapper.eq(Bookmark::getSource, query.source().trim());
        }
        wrapper.orderByDesc(Bookmark::getCreatedAt);

        Page<Bookmark> result = bookmarkMapper.selectPage(pager, wrapper);
        List<SensitiveWord> activeWords = sensitiveWordAdminService.loadActiveWords();
        List<BookmarkView> items =
                result.getRecords().stream()
                        .map(bookmark -> toView(bookmark, activeWords))
                        .toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public void deleteBookmark(BookmarkDeleteRequest request) {
        bookmarkMapper.delete(
                Wrappers.lambdaQuery(Bookmark.class)
                        .eq(Bookmark::getUserId, request.userId())
                        .eq(Bookmark::getEntityType, request.entityType())
                        .eq(Bookmark::getEntityId, request.entityId()));
    }

    private BookmarkView toView(Bookmark bookmark, List<SensitiveWord> activeWords) {
        List<String> tags = readTags(bookmark.getTags());
        SensitiveWordAnalysisResult analysis =
                sensitiveWordAdminService.analyzeContent(
                        buildContentForAnalysis(bookmark.getNote(), tags), activeWords);
        boolean hasSensitive = analysis.hasSensitive();
        return new BookmarkView(
                bookmark.getUserId(),
                bookmark.getEntityType(),
                bookmark.getEntityId(),
                bookmark.getVisibility(),
                bookmark.getNote(),
                tags,
                bookmark.getSource(),
                bookmark.getCreatedAt(),
                bookmark.getUpdatedAt(),
                hasSensitive,
                hasSensitive ? analysis.hits() : List.of(),
                hasSensitive ? analysis.riskLevel() : null);
    }

    private List<String> readTags(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper
                            .getTypeFactory()
                            .constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.warn("解析收藏标签失败: {}", json, e);
            return List.of();
        }
    }

    private String buildContentForAnalysis(String note, List<String> tags) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(note)) {
            builder.append(note.strip());
        }
        if (!CollectionUtils.isEmpty(tags)) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(String.join(" ", tags));
        }
        return builder.toString();
    }
}
