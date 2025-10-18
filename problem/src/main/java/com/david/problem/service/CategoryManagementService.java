package com.david.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.problem.dto.CategoryCreateRequest;
import com.david.problem.dto.CategoryUpdateRequest;
import com.david.problem.dto.CategoryView;
import com.david.problem.dto.PageResult;
import com.david.problem.entity.Category;
import com.david.problem.entity.Problem;
import com.david.problem.exception.BusinessException;
import com.david.problem.mapper.CategoryMapper;
import com.david.problem.mapper.ProblemMapper;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CategoryManagementService {

    private final CategoryMapper categoryMapper;
    private final ProblemMapper problemMapper;

    public CategoryManagementService(CategoryMapper categoryMapper, ProblemMapper problemMapper) {
        this.categoryMapper = categoryMapper;
        this.problemMapper = problemMapper;
    }

    public PageResult<CategoryView> listCategories(
            int page, int size, @Nullable String keyword) {
        LambdaQueryWrapper<Category> query = Wrappers.lambdaQuery(Category.class);
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper ->
                            wrapper.like(Category::getCode, trimmed)
                                    .or()
                                    .like(Category::getName, trimmed));
        }
        query.orderByAsc(Category::getName);
        Page<Category> pager = new Page<>(page, size);
        Page<Category> result = categoryMapper.selectPage(pager, query);
        List<Category> categories = result.getRecords();
        List<CategoryView> items =
                categories == null || categories.isEmpty()
                        ? List.of()
                        : categories.stream().map(this::toView).toList();
        return new PageResult<>(
                items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public CategoryView getCategory(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        return toView(category);
    }

    @Transactional
    public CategoryView createCategory(CategoryCreateRequest request) {
        String code = normalizeCode(request.code());
        String name = normalizeName(request.name());
        ensureCodeUnique(code, null);

        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        categoryMapper.insert(category);
        return getCategory(category.getId());
    }

    @Transactional
    public CategoryView updateCategory(Integer categoryId, CategoryUpdateRequest request) {
        Category existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        boolean changed = false;

        if (request.code() != null) {
            String code = normalizeCode(request.code());
            if (!Objects.equals(code, existing.getCode())) {
                ensureCodeUnique(code, categoryId);
                existing.setCode(code);
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
        LambdaUpdateWrapper<Category> update =
                Wrappers.lambdaUpdate(Category.class).eq(Category::getId, categoryId);
        update.set(Category::getCode, existing.getCode());
        update.set(Category::getName, existing.getName());
        categoryMapper.update(null, update);
        return getCategory(categoryId);
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        Category existing = categoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        Long count =
                problemMapper.selectCount(
                        Wrappers.lambdaQuery(Problem.class).eq(Problem::getCategoryId, categoryId));
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "仍有题目关联该分类，无法删除");
        }
        categoryMapper.deleteById(categoryId);
    }

    private void ensureCodeUnique(String code, @Nullable Integer excludeCategoryId) {
        LambdaQueryWrapper<Category> query =
                Wrappers.lambdaQuery(Category.class).eq(Category::getCode, code);
        if (excludeCategoryId != null) {
            query.ne(Category::getId, excludeCategoryId);
        }
        Long count = categoryMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "分类编码已存在");
        }
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类编码不能为空");
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类名称不能为空");
        }
        return trimmed;
    }

    private CategoryView toView(Category category) {
        return new CategoryView(category.getId(), category.getCode(), category.getName());
    }
}
