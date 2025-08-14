package com.david.controller;

import com.david.service.ISubmissionService;
import com.david.submission.Submission;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/management/submission")
public class SubmissionManagementController {
    private final ISubmissionService submissionService;

    @PostMapping
    public ResponseResult<Void> createSubmission(@RequestBody Submission submission) {
        if (!submissionService.save(submission)) {
            return ResponseResult.fail(401, "提交记录创建失败");
        }
        return ResponseResult.success("提交记录创建成功");
    }

    @PostMapping("/callbackId")
    public ResponseResult<Long> createSubmissionThenCallback(@RequestBody Submission submission) {
        if (!submissionService.save(submission)) {
            return ResponseResult.fail(401, "提交记录创建失败");
        }
        return ResponseResult.success("提交记录更新成功", submission.getId());
    }
}
