package com.david.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.TagCreateRequest;
import com.david.problem.dto.TagUpdateRequest;
import com.david.problem.dto.TagView;
import com.david.problem.dto.PageResult;
import com.david.problem.entity.ProblemTag;
import com.david.problem.entity.Tag;
import com.david.common.http.exception.BusinessException;
import com.david.problem.mapper.ProblemTagMapper;
import com.david.problem.mapper.TagMapper;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TagManagementService {

    private final TagMapper tagMapper;
    private final ProblemTagMapper problemTagMapper;

    public TagManagementService(TagMapper tagMapper, ProblemTagMapper problemTagMapper) {
        this.tagMapper = tagMapper;
        this.problemTagMapper = problemTagMapper;
    }

    public PageResult<TagView> listTags(int page, int size, @Nullable String keyword) {
        LambdaQueryWrapper<Tag> query = Wrappers.lambdaQuery(Tag.class);
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper ->
                            wrapper.like(Tag::getSlug, trimmed)
                                    .or()
                                    .like(Tag::getName, trimmed));
        }
        query.orderByDesc(Tag::getUpdatedAt).orderByAsc(Tag::getSlug);
        Page<Tag> pager = new Page<>(page, size);
        Page<Tag> result = tagMapper.selectPage(pager, query);
        List<Tag> tags = result.getRecords();
        List<TagView> items =
                tags == null || tags.isEmpty()
                        ? List.of()
                        : tags.stream().map(this::toView).toList();
        return new PageResult<>(
                items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public TagView getTag(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "标签不存在");
        }
        return toView(tag);
    }

    @Transactional
    public TagView createTag(TagCreateRequest request) {
        String slug = normalizeSlug(request.slug());
        String name = normalizeName(request.name());
        ensureSlugUnique(slug, null);

        LocalDateTime now = LocalDateTime.now();
        Tag tag = new Tag();
        tag.setSlug(slug);
        tag.setName(name);
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);
        tagMapper.insert(tag);
        return getTag(tag.getId());
    }

    @Transactional
    public TagView updateTag(Long tagId, TagUpdateRequest request) {
        Tag existing = tagMapper.selectById(tagId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "标签不存在");
        }
        boolean changed = false;

        if (request.slug() != null) {
            String slug = normalizeSlug(request.slug());
            if (!Objects.equals(slug, existing.getSlug())) {
                ensureSlugUnique(slug, tagId);
                existing.setSlug(slug);
                changed = true;
            }
        }
        if (request.name() != null) {
            String name = normalizeName(request.name());
            if (!Objects.equals(name, existing.getName())) {
                existing.setName(name);
                changed = true;
            }
        }
        if (!changed) {
            return toView(existing);
        }
        existing.setUpdatedAt(LocalDateTime.now());
        LambdaUpdateWrapper<Tag> update =
                Wrappers.lambdaUpdate(Tag.class).eq(Tag::getId, tagId);
        update.set(Tag::getSlug, existing.getSlug());
        update.set(Tag::getName, existing.getName());
        update.set(Tag::getUpdatedAt, existing.getUpdatedAt());
        tagMapper.update(null, update);
        return getTag(tagId);
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Tag existing = tagMapper.selectById(tagId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "标签不存在");
        }
        Long relationCount =
                problemTagMapper.selectCount(
                        Wrappers.lambdaQuery(ProblemTag.class).eq(ProblemTag::getTagId, tagId));
        if (relationCount != null && relationCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "仍有题目关联该标签，无法删除");
        }
        tagMapper.deleteById(tagId);
    }

    private void ensureSlugUnique(String slug, @Nullable Long excludeTagId) {
        LambdaQueryWrapper<Tag> query = Wrappers.lambdaQuery(Tag.class).eq(Tag::getSlug, slug);
        if (excludeTagId != null) {
            query.ne(Tag::getId, excludeTagId);
        }
        Long count = tagMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "标签别名已存在");
        }
    }

    private String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }
        String trimmed = slug.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标签别名不能为空");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "标签名称不能为空");
        }
        return trimmed;
    }

    private TagView toView(Tag tag) {
        return new TagView(tag.getId(), tag.getSlug(), tag.getName(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
