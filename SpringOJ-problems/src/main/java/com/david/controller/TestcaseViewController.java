package com.david.controller;

import com.david.service.impl.TestCaseServiceImpl;
import com.david.utils.ResponseResult;
import com.david.vo.TestCaseVo;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/testcase")
public class TestcaseViewController {
    private final TestCaseServiceImpl testCaseService;

    @GetMapping("/")
    public ResponseResult<List<TestCaseVo>> getTestCaseVoByProblemId(@RequestParam Long problemId) {
        List<TestCaseVo> testCaseVos = testCaseService.getTestCaseVoByProblemId(problemId);
        if (testCaseVos == null || testCaseVos.isEmpty()) {
            return ResponseResult.fail(404, "测试用例样例不存在");
        }
        return ResponseResult.success("成功获取题目", testCaseVos);
    }
}
