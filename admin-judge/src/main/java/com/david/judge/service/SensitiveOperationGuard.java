package com.david.judge.service;

import com.david.core.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SensitiveOperationGuard {

    private final SensitiveActionValidator validator;

    public void ensureValid(Long userId, String token) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "缺少用户身份信息");
        }
        if (!validator.verifyAndConsume(userId, token)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "敏感操作验证失败");
        }
    }
}
