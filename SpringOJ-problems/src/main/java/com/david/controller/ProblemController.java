package com.david.controller;

import com.david.dto.CategoryDto;
import com.david.judge.Problem;
import com.david.judge.TestCase;
import com.david.judge.enums.CategoryType;
import com.david.service.IProblemService;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 题目前端控制器
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
    public ResponseResult<TestCase> createTestCase(@RequestBody Map<String, Object> payload) throws IOException {
        TestCase testCase = new TestCase();
        testCase.setProblemId(Long.valueOf(payload.get("problemId").toString()));

        // Generate unique filenames for the test case
        String fileIdentifier = UUID.randomUUID().toString();
        testCase.setInputFile("case_" + fileIdentifier + "_input.txt");
        testCase.setOutputFile("case_" + fileIdentifier + "_output.txt");

        testCase.setScore(Integer.valueOf(payload.get("score").toString()));
        String inputContent = payload.get("inputContent").toString();
        String outputContent = payload.get("outputContent").toString();

        TestCase createdTestCase = testCaseService.createTestCaseWithFiles(testCase, inputContent, outputContent);
        return ResponseResult.success("测试用例创建成功", createdTestCase);
    }

    /**
     * 更新测试用例
     */
    @PutMapping("/testcases/{id}")
    public ResponseResult<TestCase> updateTestCase(@PathVariable Long id, @RequestBody Map<String, Object> payload) throws IOException {
        TestCase testCase = testCaseService.getById(id);
        if (testCase == null) {
            return ResponseResult.fail(404, "测试用例不存在");
        }

        // Only update score and content, keep existing filenames
        testCase.setScore(Integer.valueOf(payload.get("score").toString()));
        String inputContent = payload.get("inputContent").toString();
        String outputContent = payload.get("outputContent").toString();

        TestCase updatedTestCase = testCaseService.updateTestCaseWithFiles(testCase, inputContent, outputContent);
        return ResponseResult.success("测试用例更新成功", updatedTestCase);
    }

    /**
     * 删除测试用例
     */
    @DeleteMapping("/testcases/{id}")
    public ResponseResult<Void> deleteTestCase(@PathVariable Long id) throws IOException {
        if (testCaseService.deleteTestCaseWithFiles(id)) {
            return ResponseResult.success("测试用例删除成功");
        }
        return ResponseResult.fail(500, "测试用例删除失败");
    }

    /**
     * 获取测试用例内容
     */
    @GetMapping("/testcases/{id}/content")
    public ResponseResult<ITestCaseService.TestCaseContent> getTestCaseContent(@PathVariable Long id) throws IOException {
        return ResponseResult.success("成功获取测试用例内容", testCaseService.getTestCaseContent(id));
    }

    /**
     * 获取所有题目类别
     */
    @GetMapping("/categories")
    public ResponseResult<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = new ArrayList<>();
        for (CategoryType type : CategoryType.values()) {
            categories.add(CategoryDto.builder().category(type.getCategory()).description(type.getDescription()).build());
        }
        return ResponseResult.success("成功获取所有题目类别", categories);
    }
}
