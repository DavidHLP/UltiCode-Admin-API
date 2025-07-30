package com.david.service.impl;

import org.springframework.stereotype.Service;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.service.IDockerExecuteService;
import com.david.template.SandboxTemplate;
import com.david.template.java.JavaDockerAcmSandbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Docker执行服务实现类
 * 使用模板方法模式将具体语言的沙箱逻辑分离
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerExecuteServiceImpl implements IDockerExecuteService {

    private final JavaDockerAcmSandbox javaDockerAcmSandbox;

    @Override
    public JudgeResult executeCode(SandboxExecuteRequest request) {
        // 根据语言类型选择对应的沙箱模板
        // 目前只实现了Java的沙箱，后续可扩展其他语言
        SandboxTemplate sandboxTemplate;
        switch (request.getLanguage()) {
            case JAVA:
                sandboxTemplate = javaDockerAcmSandbox;
                break;
            // TODO: 后续添加其余语言沙箱
            default:
                log.error("不支持的编程语言: {}", request.getLanguage());
                return createErrorResult(com.david.judge.enums.JudgeStatus.SYSTEM_ERROR, "不支持的编程语言");
        }

        return sandboxTemplate.execute(request);
    }

    private JudgeResult createErrorResult(com.david.judge.enums.JudgeStatus status, String errorMessage) {
        JudgeResult result = new JudgeResult();
        result.setStatus(status);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        result.setErrorMessage(errorMessage);
        return result;
    }
}