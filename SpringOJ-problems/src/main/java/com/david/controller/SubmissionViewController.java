package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.service.ISubmissionService;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;
import com.david.submission.vo.SubmissionDetailVo;
import com.david.utils.BaseController;
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

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/view/submission")
public class SubmissionViewController extends BaseController {
    private final ISubmissionService submissionService;

    @GetMapping("/page")
    public ResponseResult<Page<SubmissionCardVo>> pageSubmissionCardVos(
            @RequestParam @Min(1) long page, @RequestParam @Min(1) long size, @RequestParam @NotNull @Min(1) Long problemId) {
        Page<SubmissionCardVo> p = new Page<>(page, size);
        Page<SubmissionCardVo> result =
                submissionService.pageSubmissionCardVos(p, problemId, getCurrentUserId());
        return ResponseResult.success("成功获取提交分页", result);
    }

    @GetMapping("/detail")
    public ResponseResult<SubmissionDetailVo> getSubmissionDetailVoBySubmissionId(
            @RequestParam @NotNull @Min(1) Long submissionId) {
        Submission submission = submissionService.getById(submissionId);
        if (submission == null) {
            throw BizException.of(404, "提交不存在");
        }
        return ResponseResult.success(
                "成功获取提交详情",
                SubmissionDetailVo.builder()
                        .compileInfo(submission.getCompileInfo())
                        .judgeInfo(submission.getJudgeInfo())
                        .id(submission.getId())
                        .errorTestCaseExpectOutput(submission.getErrorTestCaseExpectOutput())
                        .errorTestCaseOutput(submission.getErrorTestCaseOutput())
                        .errorTestCaseId(submission.getErrorTestCaseId())
                        .language(submission.getLanguage())
                        .memoryUsed(submission.getMemoryUsed())
                        .problemId(submission.getProblemId())
                        .sourceCode(submission.getSourceCode())
                        .status(submission.getStatus())
                        .timeUsed(submission.getTimeUsed())
                        .userId(submission.getUserId())
                        .build());
    }
}
