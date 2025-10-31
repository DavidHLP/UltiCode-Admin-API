package com.david.problem.service;

import static com.david.problem.service.support.CrudSupport.*;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.CategoryCreateRequest;
import com.david.problem.dto.CategoryUpdateRequest;
import com.david.problem.dto.CategoryView;
import com.david.problem.dto.PageResult;
import com.david.problem.entity.Category;
import com.david.problem.entity.Problem;
import com.david.problem.mapper.CategoryMapper;
import com.david.problem.mapper.ProblemMapper;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class CategoryManagementService {

    private final CategoryMapper categoryMapper;
    private final ProblemMapper problemMapper;

    public CategoryManagementService(CategoryMapper categoryMapper, ProblemMapper problemMapper) {
        this.categoryMapper = categoryMapper;
        this.problemMapper = problemMapper;
    }

    public PageResult<CategoryView> listCategories(int page, int size, @Nullable String keyword) {
        var query = Wrappers.<Category>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            query.and(
                    w -> w.like(Category::getCode, trimmed).or().like(Category::getName, trimmed));
        }
        query.orderByAsc(Category::getName);

        return selectPageAndMap(categoryMapper, new Page<>(page, size), query, this::toView);
    }

    public CategoryView getCategory(Integer categoryId) {
        Category category = requireFound(categoryMapper.selectById(categoryId), "分类不存在");
        return toView(category);
    }

    @Transactional
    public CategoryView createCategory(CategoryCreateRequest request) {
        String code = normalizeRequiredLower(request.code(), "分类编码");
        String name = normalizeRequired(request.name(), "分类名称");

        // 唯一性校验（新增不排除 id）
        ensureUnique(categoryMapper, Category::getCode, code, Category::getId, null, "分类编码已存在");

        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        categoryMapper.insert(category);
        return getCategory(category.getId());
    }

    @Transactional
    public CategoryView updateCategory(Integer categoryId, CategoryUpdateRequest request) {
        Category existing = requireFound(categoryMapper.selectById(categoryId), "分类不存在");
        boolean changed = false;

        if (request.code() != null) {
            String code = normalizeRequiredLower(request.code(), "分类编码");
            if (!Objects.equals(code, existing.getCode())) {
                ensureUnique(
                        categoryMapper,
                        Category::getCode,
                        code,
                        Category::getId,
                        categoryId,
                        "分类编码已存在");
                existing.setCode(code);
                changed = true;
            }
        }
        if (request.name() != null) {
            String name = normalizeRequired(request.name(), "分类名称");
            changed |= setIfChanged(name, existing.getName(), existing::setName);
        }

        if (!changed) {
            return toView(existing);
        }

        updateById(
                categoryMapper,
                Category::getId,
                categoryId,
                uw -> {
                    uw.set(Category::getCode, existing.getCode());
                    uw.set(Category::getName, existing.getName());
                });
        return getCategory(categoryId);
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        Category existing = requireFound(categoryMapper.selectById(categoryId), "分类不存在");
        Long count =
                problemMapper.selectCount(
                        Wrappers.<Problem>lambdaQuery().eq(Problem::getCategoryId, categoryId));
        assertNoRelations(count, "仍有题目关联该分类，无法删除");
        categoryMapper.deleteById(existing.getId());
    }

    private CategoryView toView(Category category) {
        return new CategoryView(category.getId(), category.getCode(), category.getName());
    }
}
