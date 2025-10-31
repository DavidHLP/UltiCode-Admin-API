package com.david.problem.controller;

import com.david.core.http.ApiResponse;
import com.david.problem.dto.LanguageCreateRequest;
import com.david.problem.dto.LanguageUpdateRequest;
import com.david.problem.dto.LanguageView;
import com.david.problem.dto.PageResult;
import com.david.problem.service.LanguageManagementService;

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
@RequestMapping("/api/admin/languages")
@RequiredArgsConstructor
public class LanguageAdminController {

    private final LanguageManagementService languageManagementService;

    @GetMapping
    public ApiResponse<PageResult<LanguageView>> listLanguages(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        log.info(
                "查询语言列表，页码: {}, 大小: {}, 关键字: {}, 是否启用: {}",
                page,
                size,
                keyword,
                isActive);
        PageResult<LanguageView> languages =
                languageManagementService.listLanguages(page, size, keyword, isActive);
        return ApiResponse.success(languages);
}

    @GetMapping("/{languageId}")
    public ApiResponse<LanguageView> getLanguage(@PathVariable Integer languageId) {
        log.info("获取语言详情，语言ID: {}", languageId);
        LanguageView language = languageManagementService.getLanguage(languageId);
        return ApiResponse.success(language);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LanguageView> createLanguage(
            @Valid @RequestBody LanguageCreateRequest request) {
        log.info("创建语言，请求参数: {}", request);
        LanguageView language = languageManagementService.createLanguage(request);
        log.info("创建语言成功，语言ID: {}", language.id());
        return ApiResponse.success(language);
    }

    @PutMapping("/{languageId}")
    public ApiResponse<LanguageView> updateLanguage(
            @PathVariable Integer languageId,
            @Valid @RequestBody LanguageUpdateRequest request) {
        log.info("更新语言，语言ID: {}", languageId);
        LanguageView language = languageManagementService.updateLanguage(languageId, request);
        log.info("更新语言成功，语言ID: {}", language.id());
        return ApiResponse.success(language);
    }

    @DeleteMapping("/{languageId}")
    public ApiResponse<Void> deleteLanguage(@PathVariable Integer languageId) {
        log.info("删除语言，语言ID: {}", languageId);
        languageManagementService.deleteLanguage(languageId);
        log.info("删除语言成功，语言ID: {}", languageId);
        return ApiResponse.success(null);
    }
}
