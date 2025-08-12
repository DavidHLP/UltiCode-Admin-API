package com.david.controller;

import com.david.judge.TestCase;
import com.david.service.impl.TestCaseServiceImpl;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/management/testcase")
public class TestcaseManagementController {
    private final TestCaseServiceImpl testCaseService;

    /** 根据题目ID获取所有测试用例 */
    @GetMapping("/{problemId}")
    public ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable Long problemId) {
        List<TestCase> testCases = testCaseService.getList(problemId);
        return ResponseResult.success("成功获取测试用例", testCases);
    }

    /** 创建测试用例 */
    @PostMapping
    public ResponseResult<TestCase> createTestCase(@RequestBody TestCase testCase) {
        if (testCaseService.save(testCase)) {
            return ResponseResult.success("测试用例创建成功", testCase);
        }
        return ResponseResult.fail(500, "测试用例创建失败");
    }

    /** 更新测试用例 */
    @PutMapping
    public ResponseResult<TestCase> updateTestCase(@RequestBody TestCase testCase) {
        if (testCaseService.update(testCase)) {
            return ResponseResult.success("测试用例更新成功", testCase);
        }
        return ResponseResult.fail(500, "测试用例更新失败");
    }

    /** 删除测试用例 */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTestCase(@PathVariable Long id) {
        if (testCaseService.delete(id)) {
            return ResponseResult.success("测试用例删除成功");
        }
        return ResponseResult.fail(500, "测试用例删除失败");
    }
}
