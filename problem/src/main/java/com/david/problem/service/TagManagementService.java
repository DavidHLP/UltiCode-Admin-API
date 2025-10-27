package com.david.problem.service;

import static com.david.problem.service.support.CrudSupport.*;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.PageResult;
import com.david.problem.dto.TagCreateRequest;
import com.david.problem.dto.TagUpdateRequest;
import com.david.problem.dto.TagView;
import com.david.problem.entity.ProblemTag;
import com.david.problem.entity.Tag;
import com.david.problem.mapper.ProblemTagMapper;
import com.david.problem.mapper.TagMapper;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TagManagementService {

    private final TagMapper tagMapper;
    private final ProblemTagMapper problemTagMapper;

    public TagManagementService(TagMapper tagMapper, ProblemTagMapper problemTagMapper) {
        this.tagMapper = tagMapper;
        this.problemTagMapper = problemTagMapper;
    }

    public PageResult<TagView> listTags(int page, int size, @Nullable String keyword) {
        var query = Wrappers.<Tag>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(Tag::getSlug, trimmed).or().like(Tag::getName, trimmed));
        }
        query.orderByDesc(Tag::getUpdatedAt).orderByAsc(Tag::getSlug);

        return selectPageAndMap(tagMapper, new Page<>(page, size), query, this::toView);
    }

    public TagView getTag(Long tagId) {
        Tag tag = requireFound(tagMapper.selectById(tagId), "标签不存在");
        return toView(tag);
    }

    @Transactional
    public TagView createTag(TagCreateRequest request) {
        String slug = normalizeRequiredLower(request.slug(), "标签别名");
        String name = normalizeRequired(request.name(), "标签名称");

        ensureUnique(tagMapper, Tag::getSlug, slug, Tag::getId, null, "标签别名已存在");

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
        Tag existing = requireFound(tagMapper.selectById(tagId), "标签不存在");
        boolean changed = false;

        if (request.slug() != null) {
            String slug = normalizeRequiredLower(request.slug(), "标签别名");
            if (!Objects.equals(slug, existing.getSlug())) {
                ensureUnique(tagMapper, Tag::getSlug, slug, Tag::getId, tagId, "标签别名已存在");
                existing.setSlug(slug);
                changed = true;
            }
        }
        if (request.name() != null) {
            String name = normalizeRequired(request.name(), "标签名称");
            changed |= setIfChanged(name, existing.getName(), existing::setName);
        }

        if (!changed) {
            return toView(existing);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        updateById(
                tagMapper,
                Tag::getId,
                tagId,
                uw -> {
                    uw.set(Tag::getSlug, existing.getSlug());
                    uw.set(Tag::getName, existing.getName());
                    uw.set(Tag::getUpdatedAt, existing.getUpdatedAt());
                });
        return getTag(tagId);
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Tag existing = requireFound(tagMapper.selectById(tagId), "标签不存在");
        Long relationCount =
                problemTagMapper.selectCount(
                        Wrappers.<ProblemTag>lambdaQuery().eq(ProblemTag::getTagId, tagId));
        assertNoRelations(relationCount, "仍有题目关联该标签，无法删除");
        tagMapper.deleteById(existing.getId());
    }

    private TagView toView(Tag tag) {
        return new TagView(
                tag.getId(), tag.getSlug(), tag.getName(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
