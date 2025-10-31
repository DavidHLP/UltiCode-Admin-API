package com.david.problem.controller;

import com.david.core.http.ApiResponse;
import com.david.problem.dto.PageResult;
import com.david.problem.dto.TagCreateRequest;
import com.david.problem.dto.TagUpdateRequest;
import com.david.problem.dto.TagView;
import com.david.problem.service.TagManagementService;

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
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class TagAdminController {

    private final TagManagementService tagManagementService;

    @GetMapping
    public ApiResponse<PageResult<TagView>> listTags(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword) {
        log.info("查询标签列表，页码: {}, 大小: {}, 关键字: {}", page, size, keyword);
        PageResult<TagView> tags = tagManagementService.listTags(page, size, keyword);
        return ApiResponse.success(tags);
    }

    @GetMapping("/{tagId}")
    public ApiResponse<TagView> getTag(@PathVariable Long tagId) {
        log.info("获取标签详情，标签ID: {}", tagId);
        TagView tag = tagManagementService.getTag(tagId);
        return ApiResponse.success(tag);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TagView> createTag(@Valid @RequestBody TagCreateRequest request) {
        log.info("创建标签，请求参数: {}", request);
        TagView tag = tagManagementService.createTag(request);
        log.info("创建标签成功，标签ID: {}", tag.id());
        return ApiResponse.success(tag);
    }

    @PutMapping("/{tagId}")
    public ApiResponse<TagView> updateTag(
            @PathVariable Long tagId, @Valid @RequestBody TagUpdateRequest request) {
        log.info("更新标签，标签ID: {}", tagId);
        TagView tag = tagManagementService.updateTag(tagId, request);
        log.info("更新标签成功，标签ID: {}", tag.id());
        return ApiResponse.success(tag);
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Long tagId) {
        log.info("删除标签，标签ID: {}", tagId);
        tagManagementService.deleteTag(tagId);
        log.info("删除标签成功，标签ID: {}", tagId);
        return ApiResponse.success(null);
    }
}
