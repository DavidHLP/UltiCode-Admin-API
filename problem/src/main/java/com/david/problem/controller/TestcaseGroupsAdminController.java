package com.david.problem.controller;

import com.david.core.http.ApiResponse;
import com.david.problem.dto.TestcaseGroupUpsertRequest;
import com.david.problem.dto.TestcaseGroupView;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/datasets/{datasetId}/groups")
@RequiredArgsConstructor
public class TestcaseGroupsAdminController {

    private final DatasetManagementService datasetManagementService;

    @GetMapping
    public ApiResponse<List<TestcaseGroupView>> listGroups(
            @PathVariable Long datasetId,
            @RequestParam(defaultValue = "true") boolean withTestcases) {
        log.info("查询数据集 {} 的测试组，是否含用例: {}", datasetId, withTestcases);
        List<TestcaseGroupView> groups =
                datasetManagementService.listGroups(datasetId, withTestcases);
        return ApiResponse.success(groups);
    }

    @GetMapping("/{groupId}")
    public ApiResponse<TestcaseGroupView> getGroup(
            @PathVariable Long datasetId,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "true") boolean withTestcases) {
        log.info("获取数据集 {} 的测试组 {} 详情", datasetId, groupId);
        TestcaseGroupView view =
                datasetManagementService.getGroup(datasetId, groupId, withTestcases);
        return ApiResponse.success(view);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TestcaseGroupView> createGroup(
            @PathVariable Long datasetId, @Valid @RequestBody TestcaseGroupUpsertRequest request) {
        log.info("创建数据集 {} 的测试组，请求：{}", datasetId, request);
        TestcaseGroupView view = datasetManagementService.createGroup(datasetId, request);
        return ApiResponse.success(view);
    }

    @PutMapping("/{groupId}")
    public ApiResponse<TestcaseGroupView> updateGroup(
            @PathVariable Long datasetId,
            @PathVariable Long groupId,
            @Valid @RequestBody TestcaseGroupUpsertRequest request) {
        log.info("更新数据集 {} 的测试组 {}，请求：{}", datasetId, groupId, request);
        TestcaseGroupView view = datasetManagementService.updateGroup(datasetId, groupId, request);
        return ApiResponse.success(view);
    }

    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> deleteGroup(
            @PathVariable Long datasetId, @PathVariable Long groupId) {
        log.info("删除数据集 {} 的测试组 {}", datasetId, groupId);
        datasetManagementService.deleteGroup(datasetId, groupId);
        return ApiResponse.success(null);
    }
}
