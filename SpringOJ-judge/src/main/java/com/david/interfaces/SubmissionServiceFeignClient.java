package com.david.interfaces;

import com.david.judge.Submission;
import com.david.utils.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 提交记录服务Feign客户端
 */
@FeignClient(name = "problems-service", path = "/submissions/api" ,contextId = "submissionServiceFeignClient")
public interface SubmissionServiceFeignClient {

    @PostMapping
    ResponseResult<Submission> createSubmission(@RequestBody Submission submission);

    @GetMapping("/{id}")
    ResponseResult<Submission> getSubmissionById(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    ResponseResult<Void> updateSubmission(@PathVariable("id") Long id, @RequestBody Submission submission);
}
