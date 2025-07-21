package com.david.controller;

import com.david.judge.Problem;
import com.david.judge.TestCase;
import com.david.service.IProblemService;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  题目前端控制器
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@RestController
@RequestMapping("/problems/api")
@RequiredArgsConstructor
public class ProblemController {

    private final IProblemService problemService;
    private final ITestCaseService testCaseService;

    @GetMapping
    public ResponseResult<List<Problem>> getAllProblems() {
        return ResponseResult.success("成功获取所有题目", problemService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<Problem> getProblemById(@PathVariable Long id) {
        Problem problem = problemService.getById(id);
        if (problem != null) {
                        List<TestCase> testCases = testCaseService.lambdaQuery().eq(TestCase::getProblemId, id).list();
            problem.setTestCases(testCases);
        }
        return ResponseResult.success("成功获取题目", problem);
    }

    @PostMapping
    public ResponseResult<Void> createProblem(@RequestBody Problem problem) {
        if (problemService.save(problem)) {
            return ResponseResult.success("题目创建成功");
        }
        return ResponseResult.fail(500, "题目创建失败");
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> updateProblem(@PathVariable Long id, @RequestBody Problem problem) {
        problem.setId(id);
        if (problemService.updateById(problem)) {
            return ResponseResult.success("题目更新成功");
        }
        return ResponseResult.fail(500, "题目更新失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteProblem(@PathVariable Long id) {
        if (problemService.removeById(id)) {
            return ResponseResult.success("题目删除成功");
        }
        return ResponseResult.fail(500, "题目删除失败");
    }

    /**
     * 根据题目ID获取所有测试用例
     */
    @GetMapping("/testcases/problem/{problemId}")
    public ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable Long problemId) {
        List<TestCase> testCases = testCaseService.lambdaQuery().eq(TestCase::getProblemId, problemId).list();
        return ResponseResult.success("成功获取测试用例", testCases);
    }

    /**
     * 创建测试用例
     */
    @PostMapping("/testcases")
    public ResponseResult<Void> createTestCase(@RequestBody TestCase testCase) {
                if (testCaseService.save(testCase)) {
            return ResponseResult.success("测试用例创建成功");
        }
        return ResponseResult.fail(500, "测试用例创建失败");
    }

    /**
     * 更新测试用例
     */
    @PutMapping("/testcases/{id}")
    public ResponseResult<Void> updateTestCase(@PathVariable Long id, @RequestBody TestCase testCase) {
        testCase.setId(id);
        if (testCaseService.updateById(testCase)) {
            return ResponseResult.success("测试用例更新成功");
        }
        return ResponseResult.fail(500, "测试用例更新失败");
    }

    /**
     * 删除测试用例
     */
    @DeleteMapping("/testcases/{id}")
    public ResponseResult<Void> deleteTestCase(@PathVariable Long id) {
        if (testCaseService.removeById(id)) {
            return ResponseResult.success("测试用例删除成功");
        }
        return ResponseResult.fail(500, "测试用例删除失败");
    }
}