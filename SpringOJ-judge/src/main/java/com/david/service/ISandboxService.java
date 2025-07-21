package com.david.service;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;

/**
 * 沙箱服务接口 - 与SpringOJ-sandbox模块通信
 */
public interface ISandboxService {
    /**
     * 发送代码到沙箱执行
     */
    JudgeResult executeInSandbox(SandboxExecuteRequest request);
}
