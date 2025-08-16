package com.david.interfaces;

import com.david.submission.Submission;
import com.david.utils.ResponseResult;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/** 提交记录服务Feign客户端 */
@FeignClient(
        name = "problems-service",
        path = "/problems/api/management/submission",
        contextId = "submissionServiceFeignClient")
public interface SubmissionServiceFeignClient {
	@PostMapping("/callbackId")
	ResponseResult<Long> createSubmissionThenCallback(@RequestBody Submission submission);

	@PutMapping("/callbackId")
	ResponseResult<Long> updateSubmissionThenCallback(@RequestBody Submission submission);
}
