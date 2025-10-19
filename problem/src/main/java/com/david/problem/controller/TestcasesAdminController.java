package com.david.problem.controller;

import com.david.common.http.ApiResponse;
import com.david.problem.dto.TestcaseUpsertRequest;
import com.david.problem.dto.TestcaseView;
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
@RequestMapping("/api/admin/groups/{groupId}/testcases")
@RequiredArgsConstructor
public class TestcasesAdminController {

    private final DatasetManagementService datasetManagementService;

    @GetMapping
    public ApiResponse<List<TestcaseView>> listTestcases(@PathVariable Long groupId) {
        log.info("查询测试组 {} 的测试用例", groupId);
        List<TestcaseView> testcases = datasetManagementService.listTestcases(groupId);
        return ApiResponse.success(testcases);
    }

    @GetMapping("/{testcaseId}")
    public ApiResponse<TestcaseView> getTestcase(
            @PathVariable Long groupId, @PathVariable Long testcaseId) {
        log.info("获取测试组 {} 的测试用例 {}", groupId, testcaseId);
        TestcaseView view = datasetManagementService.getTestcase(groupId, testcaseId);
        return ApiResponse.success(view);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TestcaseView> createTestcase(
            @PathVariable Long groupId, @Valid @RequestBody TestcaseUpsertRequest request) {
        log.info("创建测试组 {} 的测试用例，请求：{}", groupId, request);
        TestcaseView view = datasetManagementService.createTestcase(groupId, request);
        return ApiResponse.success(view);
    }

    @PutMapping("/{testcaseId}")
    public ApiResponse<TestcaseView> updateTestcase(
            @PathVariable Long groupId,
            @PathVariable Long testcaseId,
            @Valid @RequestBody TestcaseUpsertRequest request) {
        log.info("更新测试组 {} 的测试用例 {}，请求：{}", groupId, testcaseId, request);
        TestcaseView view = datasetManagementService.updateTestcase(groupId, testcaseId, request);
        return ApiResponse.success(view);
    }

    @DeleteMapping("/{testcaseId}")
    public ApiResponse<Void> deleteTestcase(
            @PathVariable Long groupId, @PathVariable Long testcaseId) {
        log.info("删除测试组 {} 的测试用例 {}", groupId, testcaseId);
        datasetManagementService.deleteTestcase(groupId, testcaseId);
        return ApiResponse.success(null);
    }
}
