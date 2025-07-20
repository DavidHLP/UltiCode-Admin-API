package com.david.controller;

import com.david.service.JudgeService;
import com.david.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/judge")
public class JudgeController {

    @Autowired
    private JudgeService judgeService;

    @PostMapping("/submission/{submissionId}")
    public ResponseResult<String> judgeSubmission(@PathVariable long submissionId) {
        // 使用CompletableFuture异步执行判题，避免阻塞HTTP请求
        CompletableFuture.runAsync(() -> judgeService.doJudge(submissionId));
        return ResponseResult.success("提交 " + submissionId + " 已接收，正在处理中。");
    }
}
