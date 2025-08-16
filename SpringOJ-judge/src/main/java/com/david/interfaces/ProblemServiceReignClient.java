package com.david.interfaces;


import com.david.submission.dto.CompareDescription;
import com.david.utils.ResponseResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
		name = "problems-service",
		path = "/problems/api/management/problem",
		contextId = "problemServiceReignClient")
public interface ProblemServiceReignClient {

	/**
	 * 获取题目对比信息（函数名、难度等）。
	 *
	 * @param problemId 题目ID，不能为 null，对应后端管理端接口请求参数名为 id
	 * @return 包装的 {@link CompareDescription} 结果
	 */
	@GetMapping("/compareDescription")
	ResponseResult<CompareDescription> getCompareDescription(@RequestParam("id") Long problemId);
}
