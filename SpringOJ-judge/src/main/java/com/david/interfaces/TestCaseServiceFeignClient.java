package com.david.interfaces;

import com.david.testcase.TestCase;
import com.david.utils.ResponseResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/** 题目服务Feign客户端 */
@FeignClient(name = "problems-service", path = "/api/problems/management/testcase", contextId = "testCaseServiceFeignClient")
public interface TestCaseServiceFeignClient {
    @GetMapping("/{problemId}")
    ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable Long problemId);
}
