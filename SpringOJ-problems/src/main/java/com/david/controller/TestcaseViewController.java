package com.david.controller;

import com.david.service.impl.TestCaseServiceImpl;
import com.david.testcase.vo.TestCaseVo;
import com.david.utils.ResponseResult;
import com.david.exception.BizException;

import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/view/testcase")
public class TestcaseViewController {
    private final TestCaseServiceImpl testCaseService;

    @GetMapping("/")
    public ResponseResult<List<TestCaseVo>> getTestCaseVoByProblemId(@RequestParam @NotNull @Min(1) Long problemId) {
        List<TestCaseVo> testCaseVos = testCaseService.getTestCaseVoByProblemId(problemId);
        if (testCaseVos == null || testCaseVos.isEmpty()) {
            throw BizException.of(404, "测试用例样例不存在");
        }
        return ResponseResult.success("成功获取题目", testCaseVos);
    }
}
