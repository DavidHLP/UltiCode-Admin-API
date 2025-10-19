package com.david.interaction.controller;

import com.david.common.http.ApiResponse;
import com.david.interaction.dto.PageResult;
import com.david.interaction.dto.SensitiveWordQuery;
import com.david.interaction.dto.SensitiveWordUpsertRequest;
import com.david.interaction.dto.SensitiveWordView;
import com.david.interaction.service.SensitiveWordAdminService;
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
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/interaction/sensitive-words")
public class SensitiveWordAdminController {

    private final SensitiveWordAdminService sensitiveWordAdminService;

    @GetMapping
    public ApiResponse<PageResult<SensitiveWordView>> listSensitiveWords(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean active) {
        SensitiveWordQuery query =
                new SensitiveWordQuery(page, size, keyword, category, level, active);
        log.info("查询敏感词列表: {}", query);
        PageResult<SensitiveWordView> result =
                sensitiveWordAdminService.listSensitiveWords(query);
        return ApiResponse.success(result);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SensitiveWordView> createSensitiveWord(
            @Valid @RequestBody SensitiveWordUpsertRequest request) {
        log.info("创建敏感词: {}", request.word());
        SensitiveWordView view = sensitiveWordAdminService.createSensitiveWord(request);
        return ApiResponse.success(view);
    }

    @PutMapping("/{id}")
    public ApiResponse<SensitiveWordView> updateSensitiveWord(
            @PathVariable Long id, @Valid @RequestBody SensitiveWordUpsertRequest request) {
        log.info("更新敏感词 id={}", id);
        SensitiveWordView view = sensitiveWordAdminService.updateSensitiveWord(id, request);
        return ApiResponse.success(view);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSensitiveWord(@PathVariable Long id) {
        log.info("删除敏感词 id={}", id);
        sensitiveWordAdminService.deleteSensitiveWord(id);
        return ApiResponse.success(null);
    }
}

