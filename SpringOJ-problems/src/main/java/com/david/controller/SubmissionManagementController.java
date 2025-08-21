package com.david.controller;

import com.david.service.ISubmissionService;
import com.david.submission.Submission;
import com.david.utils.ResponseResult;
import com.david.exception.BizException;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/management/submission")
public class SubmissionManagementController {
    private final ISubmissionService submissionService;

    @PostMapping
    public ResponseResult<Void> createSubmission(@RequestBody @Valid Submission submission) {
        if (!submissionService.save(submission)) {
            throw BizException.of(401, "提交记录创建失败");
        }
        return ResponseResult.success("提交记录创建成功");
    }

    @PostMapping("/callbackId")
    public ResponseResult<Long> createSubmissionThenCallback(@RequestBody @Valid Submission submission) {
        if (!submissionService.save(submission)) {
            throw BizException.of(401, "提交记录创建失败");
        }
        return ResponseResult.success("提交记录更新成功", submission.getId());
    }

    @PutMapping("/callbackId")
    public ResponseResult<Long> updateSubmissionThenCallback(@RequestBody @Valid Submission submission) {
        if (!submissionService.updateById(submission)) throw BizException.of(401, "提交记录更新失败");
        return ResponseResult.success("提交记录更新成功", submission.getId());
    }
}
