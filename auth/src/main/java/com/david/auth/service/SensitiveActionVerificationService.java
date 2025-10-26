package com.david.auth.service;

import com.david.auth.config.AppProperties;
import com.david.auth.entity.User;
import com.david.core.exception.BusinessException;
import com.david.core.security.SensitiveDataMasker;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 负责为敏感操作发送与校验邮箱验证码。
 */
@Slf4j
@Service
public class SensitiveActionVerificationService {

    private static final String CODE_KEY_PATTERN = "auth:sensitive:code:%s";
    private static final String LOCK_KEY_PATTERN = "auth:sensitive:code:lock:%s";
    private static final Duration RESEND_LOCK_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String defaultFromAddress;

    public SensitiveActionVerificationService(
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

    public void sendVerificationCode(User user) {
        if (user == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户不存在");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先绑定邮箱");
        }
        String lockKey = LOCK_KEY_PATTERN.formatted(user.getId());
        boolean acquiredLock = Boolean.TRUE
                .equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", RESEND_LOCK_TTL));
        if (!acquiredLock) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "验证码请求过于频繁");
        }

        String code = generateCode();
        Duration ttl = appProperties.getMail().getSensitiveActionCodeTtl();
        String cacheKey = CODE_KEY_PATTERN.formatted(user.getId());

        redisTemplate.opsForValue().set(cacheKey, code, ttl);
        try {
            sendHtmlEmail(user.getEmail(), code, ttl);
            log.info(
                    "已向 {} 发送敏感操作验证码",
                    SensitiveDataMasker.maskEmail(user.getEmail()));
        } catch (MailException | MessagingException ex) {
            redisTemplate.delete(cacheKey);
            redisTemplate.delete(lockKey);
            log.error("发送敏感操作验证码失败", ex);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "发送验证码失败，请稍后重试");
        }
    }

    public void verifyCode(Long userId, String code) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "缺少用户信息");
        }
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请输入验证码");
        }
        String cacheKey = CODE_KEY_PATTERN.formatted(userId);
        String cachedCode = redisTemplate.opsForValue().get(cacheKey);
        if (!StringUtils.hasText(cachedCode) || !code.equals(cachedCode)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "验证码错误或已过期");
        }
        redisTemplate.delete(cacheKey);
    }

    private void sendHtmlEmail(String email, String code, Duration ttl)
            throws MessagingException {
        long ttlMinutes = Math.max(1, ttl.toMinutes());
        String htmlContent = String.format(appProperties.getMail().getSensitiveActionTemplate(), code, ttlMinutes);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        if (StringUtils.hasText(defaultFromAddress)) {
            helper.setFrom(defaultFromAddress);
        }
        helper.setTo(email);
        helper.setSubject(appProperties.getMail().getSensitiveActionSubject());
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private String generateCode() {
        int value = secureRandom.nextInt(900_000) + 100_000;
        return Integer.toString(value);
    }
}
