package com.david.controller;

import com.david.entity.TestCase;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@RestController
@RequestMapping("/testcases/api")
@RequiredArgsConstructor
public class TestCaseController {

    private final ITestCaseService testCaseService;

    @GetMapping("/problem/{problemId}")
    public ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable Long problemId) {
        List<TestCase> testCases = testCaseService.lambdaQuery().eq(TestCase::getProblemId, problemId).list();
        return ResponseResult.success("成功获取测试用例", testCases);
    }

    @PostMapping
    public ResponseResult<Void> createTestCase(@RequestBody TestCase testCase) {
        if (testCaseService.save(testCase)) {
            return ResponseResult.success("测试用例创建成功");
        }
        return ResponseResult.fail(500, "测试用例创建失败");
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> updateTestCase(@PathVariable Long id, @RequestBody TestCase testCase) {
        testCase.setId(id);
        if (testCaseService.updateById(testCase)) {
            return ResponseResult.success("测试用例更新成功");
        }
        return ResponseResult.fail(500, "测试用例更新失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteTestCase(@PathVariable Long id) {
        if (testCaseService.removeById(id)) {
            return ResponseResult.success("测试用例删除成功");
        }
        return ResponseResult.fail(500, "测试用例删除失败");
    }
}
