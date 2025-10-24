package com.david.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class CaptchaService {

    public void verify(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("缺少验证码令牌");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码校验失败");
        }
        // 预留第三方验证码接入点，目前仅校验非空。
    }
}
