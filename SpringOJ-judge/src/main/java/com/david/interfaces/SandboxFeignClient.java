package com.david.interfaces;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.utils.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 沙箱服务Feign客户端
 */
@FeignClient(name = "sandbox-service", path = "/api/sandbox")
public interface SandboxFeignClient {
    
    /**
     * 执行代码
     */
    @PostMapping("/execute")
    ResponseResult<JudgeResult> executeCode(@RequestBody SandboxExecuteRequest request);
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    ResponseResult<String> health();
}
