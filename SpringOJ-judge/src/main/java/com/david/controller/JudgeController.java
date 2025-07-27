package com.david.controller;

import com.david.dto.SubmitCodeRequest;
import com.david.judge.Submission;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.service.IJudgeService;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 判题控制器
 */
@RestController
@RequestMapping("/judge/api")
@RequiredArgsConstructor
public class JudgeController extends BaseController {

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
            submission.setUserId(getCurrentUserId());
            submission.setSourceCode(request.getSourceCode());
            submission.setLanguage(request.getLanguage());
            ResponseResult<Submission> submissionResponse = submissionService.createSubmission(submission);
            if (submissionResponse.getCode() != 200 || submissionResponse.getData() == null) {
                return ResponseResult.fail(500, "创建提交记录失败");
            }
            Long submissionId = submissionResponse.getData().getId();

            // 同步执行判题
            judgeService.judge(submissionId);

            return ResponseResult.success("代码提交成功", submissionId);
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
