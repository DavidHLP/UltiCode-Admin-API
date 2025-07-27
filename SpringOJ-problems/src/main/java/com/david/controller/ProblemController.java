package com.david.controller;

import com.david.dto.CategoryDto;
import com.david.dto.ProblemDto;
import com.david.dto.TestCaseDto;
import com.david.judge.CodeTemplate;
import com.david.judge.Problem;
import com.david.judge.TestCase;
import com.david.judge.enums.CategoryType;
import com.david.service.IProblemService;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public ResponseResult<ProblemDto> getProblemById(@PathVariable Long id) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            return ResponseResult.fail(404, "题目不存在");
        }
        ProblemDto problemDto = new ProblemDto();
        BeanUtils.copyProperties(problem, problemDto);
        List<TestCase> testCases = testCaseService.lambdaQuery().eq(TestCase::getProblemId, id).list();
        if (testCases.isEmpty()){
            return ResponseResult.fail(404, "测试用例不存在");
        }
        List<TestCaseDto> testCaseDto = new ArrayList<>();
        for (TestCase testCase : testCases) {
            TestCaseDto dto = new TestCaseDto();
            BeanUtils.copyProperties(testCase, dto);
            testCaseDto.add(dto);
        }
        problemDto.setTestCases(testCaseDto);
        problemDto.setInitialCode(Map.of(
                "java", CodeTemplate.JAVA_CODE_TEMPLATE
        ));
        return ResponseResult.success("成功获取题目", problemDto);
    }


    @PostMapping
    public ResponseResult<Void> createProblem(@RequestBody Problem problem) {
        if (problemService.save(problem)) {
            return ResponseResult.success("题目创建成功");
        }
        return ResponseResult.fail(500, "题目创建失败");
    }

    @PutMapping
    public ResponseResult<Void> updateProblem(@RequestBody Problem problem) {
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
    public ResponseResult<List<TestCaseDto>> getTestCasesByProblemId(@PathVariable Long problemId) {
        List<TestCase> testCases = testCaseService.lambdaQuery().eq(TestCase::getProblemId, problemId).list();
        List<TestCaseDto> testCaseDto = testCases.stream()
                .map(testCase -> {
                    TestCaseDto dto = new TestCaseDto();
                    BeanUtils.copyProperties(testCase, dto);
                    return dto;
                })
                .toList();
        return ResponseResult.success("成功获取测试用例", testCaseDto);
    }

    /**
     * 创建测试用例
     */
    @PostMapping("/testcases")
    public ResponseResult<TestCaseDto> createTestCase(@RequestBody TestCaseDto testCase) {
        if (testCaseService.save(testCase)) {
            return ResponseResult.success("测试用例创建成功", testCase);
        }
        return ResponseResult.fail(500, "测试用例创建失败");
    }

    /**
     * 更新测试用例
     */
    @PutMapping("/testcases")
    public ResponseResult<TestCaseDto> updateTestCase(@RequestBody TestCaseDto testCase) {
        if (testCaseService.updateById(testCase)) {
            return ResponseResult.success("测试用例更新成功", testCase);
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
