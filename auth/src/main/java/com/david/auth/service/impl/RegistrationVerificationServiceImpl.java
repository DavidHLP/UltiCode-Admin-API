package com.david.auth.service.impl;

import com.david.auth.config.AppProperties;
import com.david.auth.service.RegistrationVerificationService;
import com.david.auth.support.EmailSupport;
import com.david.core.exception.BusinessException;

import jakarta.mail.MessagingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
public class RegistrationVerificationServiceImpl implements RegistrationVerificationService {

    private static final String REG_CODE_KEY_PATTERN = "auth:registration:code:%s";
    private static final String REG_CODE_LOCK_PATTERN = "auth:registration:code:lock:%s";
    private static final Duration RESEND_LOCK_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;
    private final EmailSupport emailSupport;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegistrationVerificationServiceImpl(
            StringRedisTemplate redisTemplate,
            AppProperties appProperties,
            EmailSupport emailSupport) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
        this.emailSupport = emailSupport;
    }

    @Override
    public void sendRegistrationCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Email must not be blank");
        }

        String lockKey = buildLockKey(email);
        boolean acquiredLock =
                Boolean.TRUE.equals(
                        redisTemplate.opsForValue().setIfAbsent(lockKey, "1", RESEND_LOCK_TTL));
        if (!acquiredLock) {
            throw new BusinessException(
                    HttpStatus.TOO_MANY_REQUESTS, "Verification code requested too frequently");
        }

        String code = generateCode();
        Duration ttl = appProperties.getMail().getVerificationCodeTtl();
        String cacheKey = buildCacheKey(email);

        redisTemplate.opsForValue().set(cacheKey, code, ttl);

        try {
            long ttlMinutes = Math.max(1, ttl.toMinutes());
            String template = appProperties.getMail().getVerificationTemplate();
            String htmlContent = emailSupport.render(template, code, ttlMinutes);

            emailSupport.sendHtml(
                    email, appProperties.getMail().getVerificationSubject(), htmlContent);
            log.debug("Sent registration verification code to {}", email);

        } catch (MailException | MessagingException ex) {
            // 发送失败：回滚缓存与限流锁
            redisTemplate.delete(cacheKey);
            redisTemplate.delete(lockKey);
            log.error("Failed to send verification email to {}", email, ex);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification email");
        }
    }

    @Override
    public void verifyRegistrationCode(String email, String verificationCode) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(verificationCode)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST, "Email and verification code must not be blank");
        }
        String cacheKey = buildCacheKey(email);
        String cachedCode = redisTemplate.opsForValue().get(cacheKey);
        if (!verificationCode.equals(cachedCode)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST, "Invalid or expired verification code");
        }
        redisTemplate.delete(cacheKey);
    }

    private String buildCacheKey(String email) {
        return REG_CODE_KEY_PATTERN.formatted(email.toLowerCase());
    }

    private String buildLockKey(String email) {
        return REG_CODE_LOCK_PATTERN.formatted(email.toLowerCase());
    }

    private String generateCode() {
        int value = secureRandom.nextInt(900_000) + 100_000;
        return Integer.toString(value);
    }
}
