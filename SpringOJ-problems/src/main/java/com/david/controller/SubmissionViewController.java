package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.service.ISubmissionService;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;
import com.david.submission.vo.SubmissionDetailVo;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/submission")
public class SubmissionViewController extends BaseController {
    private final ISubmissionService submissionService;

    @GetMapping("/page")
    public ResponseResult<Page<SubmissionCardVo>> pageSubmissionCardVos(
            @RequestParam long page, @RequestParam long size, @RequestParam Long problemId) {
        Page<SubmissionCardVo> p = new Page<>(page, size);
        Page<SubmissionCardVo> result =
                submissionService.pageSubmissionCardVos(p, problemId, getCurrentUserId());
        return ResponseResult.success("成功获取提交分页", result);
    }

    @GetMapping("/detail")
    public ResponseResult<SubmissionDetailVo> getSubmissionDetailVoBySubmissionId(
            @RequestParam Long submissionId) {
        Submission submission = submissionService.getById(submissionId);
        if (submission == null) {
            return ResponseResult.fail(404, "提交不存在");
        }
        return ResponseResult.success(
                "成功获取提交详情",
                SubmissionDetailVo.builder()
                        .compileInfo(submission.getCompileInfo())
                        .judgeInfo(submission.getJudgeInfo())
                        .id(submission.getId())
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
