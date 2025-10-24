package com.david.admin.service;

import com.david.admin.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SensitiveOperationGuard {

    private final SensitiveActionValidator validator;

    public SensitiveOperationGuard(SensitiveActionValidator validator) {
        this.validator = validator;
    }

    public void ensureValid(Long userId, String token) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "缺少用户身份信息");
        }
        if (!validator.verifyAndConsume(userId, token)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "敏感操作验证失败");
        }
    }
}
