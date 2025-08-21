package com.david.controller;

import com.david.service.impl.TestCaseServiceImpl;
import com.david.testcase.TestCase;
import com.david.utils.ResponseResult;
import com.david.exception.BizException;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/management/testcase")
public class TestcaseManagementController {
    private final TestCaseServiceImpl testCaseService;

    /** 根据题目ID获取所有测试用例 */
    @GetMapping("/{problemId}")
    public ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable @NotNull @Min(1) Long problemId) {
        List<TestCase> testCases = testCaseService.getList(problemId);
        return ResponseResult.success("成功获取测试用例", testCases);
    }

    /** 创建测试用例 */
    @PostMapping
    public ResponseResult<TestCase> createTestCase(@RequestBody @Valid TestCase testCase) {
        if (testCaseService.save(testCase)) {
            return ResponseResult.success("测试用例创建成功", testCase);
        }
        throw BizException.of(500, "测试用例创建失败");
    }

    /** 更新测试用例 */
    @PutMapping
    public ResponseResult<TestCase> updateTestCase(@RequestBody @Valid TestCase testCase) {
        if (testCaseService.update(testCase)) {
            return ResponseResult.success("测试用例更新成功", testCase);
        }
        throw BizException.of(500, "测试用例更新失败");
    }

    /** 删除测试用例 */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTestCase(@PathVariable @NotNull @Min(1) Long id) {
        if (testCaseService.delete(id)) {
            return ResponseResult.success("测试用例删除成功");
        }
        throw BizException.of(500, "测试用例删除失败");
    }
}
