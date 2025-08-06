package com.david.service.impl;

import org.springframework.stereotype.Service;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.JudgeStatus;
import com.david.service.IDockerExecuteService;
import com.david.template.java.JavaDockerAcmSandbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Docker执行服务实现类
 * 使用策略模式根据语言类型选择对应的沙箱实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerExecuteServiceImpl implements IDockerExecuteService {

    private final JavaDockerAcmSandbox javaDockerAcmSandbox;

    @Override
    public JudgeResult executeCode(SandboxExecuteRequest request) {
        // 内联getSandboxTemplate逻辑，使用switch表达式简化语��映射
        return switch (request.getLanguage()) {
            case JAVA -> javaDockerAcmSandbox.execute(request);
	        default -> {
		        log.error("不支持的编程语言: {}", request.getLanguage());
		        // 内联createUnsupportedLanguageError逻辑，因为只使用一次
		        yield JudgeResult.builder()
				        .status(JudgeStatus.SYSTEM_ERROR)
				        .score(0)
				        .timeUsed(0)
				        .memoryUsed(0)
				        .errorMessage("不支持的编程语言: " + request.getLanguage())
				        .build();
	        }
        };
    }
}