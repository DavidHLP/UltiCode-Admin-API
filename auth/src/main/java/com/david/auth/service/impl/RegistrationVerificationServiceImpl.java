package com.david.auth.service.impl;

import com.david.auth.config.AppProperties;
import com.david.auth.exception.BusinessException;
import com.david.auth.service.RegistrationVerificationService;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class RegistrationVerificationServiceImpl implements RegistrationVerificationService {

    private static final String REG_CODE_KEY_PATTERN = "auth:registration:code:%s";
    private static final String REG_CODE_LOCK_PATTERN = "auth:registration:code:lock:%s";
    private static final Duration RESEND_LOCK_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String defaultFromAddress;

    public RegistrationVerificationServiceImpl(
            StringRedisTemplate redisTemplate, JavaMailSender mailSender, AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.appProperties = appProperties;
        if (mailSender instanceof JavaMailSenderImpl senderImpl) {
            this.defaultFromAddress = senderImpl.getUsername();
        } else {
            this.defaultFromAddress = null;
        }
    }

    @Override
    public void sendRegistrationCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Email must not be blank");
        }

        String lockKey = buildLockKey(email);
        boolean acquiredLock =
                Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", RESEND_LOCK_TTL));
        if (!acquiredLock) {
            throw new BusinessException(
                    HttpStatus.TOO_MANY_REQUESTS, "Verification code requested too frequently");
        }

        String code = generateCode();
        Duration ttl = appProperties.getMail().getVerificationCodeTtl();
        String cacheKey = buildCacheKey(email);

        redisTemplate.opsForValue().set(cacheKey, code, ttl);
        try {
            sendHtmlEmail(email, code, ttl);
            log.debug("Sent registration verification code to {}", email);
        } catch (MailException | MessagingException ex) {
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

    private void sendHtmlEmail(String email, String code, Duration ttl)
            throws MessagingException {
        String template = appProperties.getMail().getVerificationTemplate();
        long ttlMinutes = Math.max(1, ttl.toMinutes());
        String htmlContent = String.format(template, code, ttlMinutes);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        if (StringUtils.hasText(defaultFromAddress)) {
            helper.setFrom(defaultFromAddress);
        }
        helper.setTo(email);
        helper.setSubject(appProperties.getMail().getVerificationSubject());
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
