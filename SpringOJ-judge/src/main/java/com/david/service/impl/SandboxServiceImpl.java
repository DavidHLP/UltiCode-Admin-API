package com.david.service.impl;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.interfaces.SandboxFeignClient;
import com.david.service.ISandboxService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 沙箱服务实现类 - 通过OpenFeign与SpringOJ-sandbox通信
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxServiceImpl implements ISandboxService {

    private final SandboxFeignClient sandboxFeignClient;

    @Override
    public JudgeResult executeInSandbox(SandboxExecuteRequest request) {
        try {
            log.info("通过Feign发送代码到沙箱执行: submissionId={}", request.getSubmissionId());

            ResponseResult<JudgeResult> response = sandboxFeignClient.executeCode(request);

            if (response.getCode() == 200 && response.getData() != null) {
                log.info("沙箱执行完成: submissionId={}, status={}",
                        request.getSubmissionId(), response.getData().getStatus());
                return response.getData();
            } else {
                log.error("沙箱执行失败: submissionId={}, message={}",
                        request.getSubmissionId(), response.getMessage());
                return createErrorResult("沙箱执行失败: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("调用沙箱服务异常: submissionId={}", request.getSubmissionId(), e);
            return createErrorResult("沙箱服务异常: " + e.getMessage());
        }
    }

    /**
     * 健康检查沙箱服务
     */
    public boolean isHealthy() {
        try {
            ResponseResult<String> response = sandboxFeignClient.health();
            return response.getCode() == 200;
        } catch (Exception e) {
            log.warn("沙箱服务健康检查失败", e);
            return false;
        }
    }

    private JudgeResult createErrorResult(String errorMessage) {
        JudgeResult result = new JudgeResult();
        result.setStatus(com.david.judge.enums.JudgeStatus.SYSTEM_ERROR);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
