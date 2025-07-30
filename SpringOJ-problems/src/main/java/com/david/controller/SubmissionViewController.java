package com.david.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.david.service.ISubmissionService;
import com.david.utils.ResponseResult;
import com.david.vo.SubmissionVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/submissions/api/view")
public class SubmissionViewController {

    private final ISubmissionService submissionService;

    @GetMapping("/problem/{problemId}")
    public ResponseResult<List<SubmissionVo>> getSubmissionsByProblemId(@PathVariable Long problemId) {
        List<SubmissionVo> submissions = submissionService.lambdaQuery()
                .eq(com.david.judge.Submission::getProblemId, problemId)
                .list()
                .stream()
                .map(submission -> {
                    SubmissionVo dto = new SubmissionVo();
                    org.springframework.beans.BeanUtils.copyProperties(submission, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseResult.success("成功获取提交记录", submissions);
    }
}
