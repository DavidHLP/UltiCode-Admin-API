package com.david.problem.controller;

import com.david.common.http.ApiResponse;
import com.david.problem.dto.DatasetDetailView;
import com.david.problem.dto.DatasetUpsertRequest;
import com.david.problem.service.DatasetManagementService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/problems/{problemId}/datasets")
@RequiredArgsConstructor
public class DatasetsAdminController {

    private final DatasetManagementService datasetManagementService;

    @GetMapping
    public ApiResponse<List<DatasetDetailView>> listDatasets(@PathVariable Long problemId) {
        log.info("查询题目 {} 的数据集列表", problemId);
        List<DatasetDetailView> datasets = datasetManagementService.listProblemDatasets(problemId);
        return ApiResponse.success(datasets);
    }

    @GetMapping("/{datasetId}")
    public ApiResponse<DatasetDetailView> getDataset(
            @PathVariable Long problemId, @PathVariable Long datasetId) {
        log.info("获取题目 {} 的数据集 {} 详情", problemId, datasetId);
        DatasetDetailView detail = datasetManagementService.getDataset(problemId, datasetId);
        return ApiResponse.success(detail);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DatasetDetailView> createDataset(
            @PathVariable Long problemId, @Valid @RequestBody DatasetUpsertRequest request) {
        log.info("为题目 {} 创建数据集，请求：{}", problemId, request);
        DatasetDetailView detail = datasetManagementService.createDataset(problemId, request);
        log.info("创建数据集成功，ID: {}", detail.id());
        return ApiResponse.success(detail);
    }

    @PutMapping("/{datasetId}")
    public ApiResponse<DatasetDetailView> updateDataset(
            @PathVariable Long problemId,
            @PathVariable Long datasetId,
            @Valid @RequestBody DatasetUpsertRequest request) {
        log.info("更新题目 {} 的数据集 {}，请求：{}", problemId, datasetId, request);
        DatasetDetailView detail =
                datasetManagementService.updateDataset(problemId, datasetId, request);
        return ApiResponse.success(detail);
    }

    @DeleteMapping("/{datasetId}")
    public ApiResponse<Void> deleteDataset(
            @PathVariable Long problemId, @PathVariable Long datasetId) {
        log.info("删除题目 {} 的数据集 {}", problemId, datasetId);
        datasetManagementService.deleteDataset(problemId, datasetId);
        return ApiResponse.success(null);
    }
}
