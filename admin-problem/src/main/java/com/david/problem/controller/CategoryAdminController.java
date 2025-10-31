package com.david.problem.controller;

import com.david.core.http.ApiResponse;
import com.david.problem.dto.CategoryCreateRequest;
import com.david.problem.dto.CategoryUpdateRequest;
import com.david.problem.dto.CategoryView;
import com.david.problem.dto.PageResult;
import com.david.problem.service.CategoryManagementService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryManagementService categoryManagementService;

    @GetMapping
    public ApiResponse<PageResult<CategoryView>> listCategories(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword) {
        log.info("查询分类列表，页码: {}, 大小: {}, 关键字: {}", page, size, keyword);
        PageResult<CategoryView> categories =
                categoryManagementService.listCategories(page, size, keyword);
        return ApiResponse.success(categories);
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryView> getCategory(@PathVariable Integer categoryId) {
        log.info("获取分类详情，分类ID: {}", categoryId);
        CategoryView category = categoryManagementService.getCategory(categoryId);
        return ApiResponse.success(category);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryView> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        log.info("创建分类，请求参数: {}", request);
        CategoryView category = categoryManagementService.createCategory(request);
        log.info("创建分类成功，分类ID: {}", category.id());
        return ApiResponse.success(category);
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryView> updateCategory(
            @PathVariable Integer categoryId, @Valid @RequestBody CategoryUpdateRequest request) {
        log.info("更新分类，分类ID: {}", categoryId);
        CategoryView category = categoryManagementService.updateCategory(categoryId, request);
        log.info("更新分类成功，分类ID: {}", category.id());
        return ApiResponse.success(category);
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Integer categoryId) {
        log.info("删除分类，分类ID: {}", categoryId);
        categoryManagementService.deleteCategory(categoryId);
        log.info("删除分类成功，分类ID: {}", categoryId);
        return ApiResponse.success(null);
    }
}
