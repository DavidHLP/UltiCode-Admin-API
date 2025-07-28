package com.david.service;


import com.david.sandbox.dto.JudgeResult;
import com.david.sandbox.dto.SandboxExecuteRequest;

/**
 * Docker执行服务接口
 */
public interface IDockerExecuteService {
    /**
     * 在Docker容器中执行代码
     */
    JudgeResult executeCode(SandboxExecuteRequest request);
}
