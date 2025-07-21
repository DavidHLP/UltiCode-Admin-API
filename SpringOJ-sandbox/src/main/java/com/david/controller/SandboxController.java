package com.david.controller;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.service.IDockerExecuteService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 沙箱控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sandbox")
@RequiredArgsConstructor
public class SandboxController {

    private final IDockerExecuteService dockerExecuteService;

    /**
     * 执行代码
     */
    @PostMapping("/execute")
    public ResponseResult<JudgeResult> executeCode(@RequestBody SandboxExecuteRequest request) {
        try {
            log.info("接收到代码执行请求: submissionId={}, language={}",
                    request.getSubmissionId(), request.getLanguage());

            JudgeResult result = dockerExecuteService.executeCode(request);

            log.info("代码执行完成: submissionId={}, status={}, score={}",
                    request.getSubmissionId(), result.getStatus(), result.getScore());

            return ResponseResult.success("代码执行成功", result);
        } catch (Exception e) {
            log.error("代码执行异常: submissionId={}", request.getSubmissionId(), e);
            return ResponseResult.fail(500, "执行失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseResult<String> health() {
        return ResponseResult.success("健康检查成功", "Sandbox服务运行正常");
    }
}