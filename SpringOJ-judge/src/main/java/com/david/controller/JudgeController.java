package com.david.controller;

import com.david.dto.SubmitCodeRequest;
import com.david.judge.Submission;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.service.IJudgeService;
import com.david.service.impl.JudgeServiceImpl;
import com.david.utils.AsyncContextUtil;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 判题控制器
 */
@RestController
@RequestMapping("/api/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final SubmissionServiceFeignClient submissionService;
    private final IJudgeService judgeService;

    /**
     * 提交代码进行判题
     */
    @PostMapping("/submit")
    public ResponseResult<Long> submitCode(@RequestBody @Validated SubmitCodeRequest request) {
        try {
            // 创建提交记录
            Submission submission = new Submission();
            submission.setProblemId(request.getProblemId());
            submission.setUserId(request.getUserId());
            submission.setSourceCode(request.getSourceCode());
            submission.setLanguage(request.getLanguage());
            ResponseResult<Submission> submissionResponse = submissionService.createSubmission(submission);
            if (submissionResponse.getCode() != 200 || submissionResponse.getData() == null) {
                return ResponseResult.fail(500, "创建提交记录失败");
            }
            Long submissionId = submissionResponse.getData().getId();

            // 捕获当前请求的权限信息
            Map<String, String> authContext = AsyncContextUtil.captureAuthContext();

            // 异步执行判题（传递权限信息）
            if (judgeService instanceof JudgeServiceImpl) {
                ((JudgeServiceImpl) judgeService).judgeAsync(request, submissionId, authContext);
            } else {
                judgeService.judgeAsync(request, submissionId);
            }

            return ResponseResult.success("代码提交成功，正在判题中...", submissionId);
        } catch (Exception e) {
            return ResponseResult.fail(500, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 查询提交记录
     */
    @GetMapping("/submission/{submissionId}")
    public ResponseResult<Submission> getSubmission(@PathVariable Long submissionId) {
        try {
            ResponseResult<Submission> response = submissionService.getSubmissionById(submissionId);
            if (response.getCode() != 200 || response.getData() == null) {
                return ResponseResult.fail(404, "提交记录不存在");
            }
            return ResponseResult.success("提交记录获取成功", response.getData());
        } catch (Exception e) {
            return ResponseResult.fail(500, "查询失败: " + e.getMessage());
        }
    }
}
