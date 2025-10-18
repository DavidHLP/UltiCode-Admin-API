package com.david.problem.controller;

import com.david.common.http.ApiResponse;
import com.david.problem.dto.DifficultyCreateRequest;
import com.david.problem.dto.DifficultyUpdateRequest;
import com.david.problem.dto.DifficultyView;
import com.david.problem.dto.PageResult;
import com.david.problem.service.DifficultyManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/admin/difficulties")
public class DifficultyAdminController {

    private final DifficultyManagementService difficultyManagementService;

    public DifficultyAdminController(
            DifficultyManagementService difficultyManagementService) {
        this.difficultyManagementService = difficultyManagementService;
    }

    @GetMapping
    public ApiResponse<PageResult<DifficultyView>> listDifficulties(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword) {
        log.info("查询难度列表，页码: {}, 大小: {}, 关键字: {}", page, size, keyword);
        PageResult<DifficultyView> difficulties =
                difficultyManagementService.listDifficulties(page, size, keyword);
        return ApiResponse.success(difficulties);
}

    @GetMapping("/{difficultyId}")
    public ApiResponse<DifficultyView> getDifficulty(@PathVariable Integer difficultyId) {
        log.info("获取难度详情，难度ID: {}", difficultyId);
        DifficultyView difficulty = difficultyManagementService.getDifficulty(difficultyId);
        return ApiResponse.success(difficulty);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DifficultyView> createDifficulty(
            @Valid @RequestBody DifficultyCreateRequest request) {
        log.info("创建难度，请求参数: {}", request);
        DifficultyView difficulty = difficultyManagementService.createDifficulty(request);
        log.info("创建难度成功，难度ID: {}", difficulty.id());
        return ApiResponse.success(difficulty);
    }

    @PutMapping("/{difficultyId}")
    public ApiResponse<DifficultyView> updateDifficulty(
            @PathVariable Integer difficultyId,
            @Valid @RequestBody DifficultyUpdateRequest request) {
        log.info("更新难度，难度ID: {}", difficultyId);
        DifficultyView difficulty = difficultyManagementService.updateDifficulty(difficultyId, request);
        log.info("更新难度成功，难度ID: {}", difficulty.id());
        return ApiResponse.success(difficulty);
    }

    @DeleteMapping("/{difficultyId}")
    public ApiResponse<Void> deleteDifficulty(@PathVariable Integer difficultyId) {
        log.info("删除难度，难度ID: {}", difficultyId);
        difficultyManagementService.deleteDifficulty(difficultyId);
        log.info("删除难度成功，难度ID: {}", difficultyId);
        return ApiResponse.success(null);
    }
}
