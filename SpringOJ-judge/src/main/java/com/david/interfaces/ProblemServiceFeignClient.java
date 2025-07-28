package com.david.interfaces;

import com.david.judge.Problem;
import com.david.judge.TestCase;
import com.david.utils.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 题目服务Feign客户端
 */
@FeignClient(name = "problems-service", path = "/problems/api/management")
public interface ProblemServiceFeignClient {

    /**
     * 根据ID获取题目详情
     */
    @GetMapping("/{id}")
    ResponseResult<Problem> getProblemById(@PathVariable("id") Long id);

    /**
     * 根据题目ID获取所有测试用例
     */
    @GetMapping("/testcases/problem/{problemId}")
    ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable("problemId") Long problemId);
}
