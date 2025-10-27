package com.david.auth.service.impl;

import com.david.auth.config.AppProperties;
import com.david.auth.entity.User;
import com.david.auth.service.PasswordResetService;
import com.david.auth.support.EmailSupport;
import com.david.core.exception.BusinessException;

import jakarta.mail.MessagingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String TOKEN_KEY_PATTERN = "auth:password-reset:token:%s";
    private static final String USER_KEY_PATTERN = "auth:password-reset:user:%d";

    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;
    private final EmailSupport emailSupport;

    public PasswordResetServiceImpl(
            StringRedisTemplate redisTemplate,
            AppProperties appProperties,
            EmailSupport emailSupport) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
        this.emailSupport = emailSupport;
    }

    @Override
    public void sendPasswordResetEmail(User user) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User email must not be blank");
        }

        String userKey = buildUserKey(user.getId());
        String existingToken = redisTemplate.opsForValue().get(userKey);
        if (StringUtils.hasText(existingToken)) {
            redisTemplate.delete(buildTokenKey(existingToken));
        }

        String token = generateToken();
        Duration ttl = appProperties.getMail().getPasswordResetTokenTtl();
        String tokenKey = buildTokenKey(token);

        // 关联 token <-> userId
        redisTemplate.opsForValue().set(tokenKey, user.getId().toString(), ttl);
        redisTemplate.opsForValue().set(userKey, token, ttl);

        try {
            // 准备邮件内容并发送
            String urlTemplate = appProperties.getMail().getPasswordResetUrlTemplate();
            String resetUrl = String.format(urlTemplate, token);
            long ttlMinutes = Math.max(1, ttl.toMinutes());

            String template = appProperties.getMail().getPasswordResetTemplate();
            String htmlContent = emailSupport.render(template, resetUrl, ttlMinutes);

            emailSupport.sendHtml(
                    user.getEmail(),
                    appProperties.getMail().getPasswordResetSubject(),
                    htmlContent);
            log.debug("Sent password reset email to {}", user.getEmail());

        } catch (MailException | MessagingException ex) {
            // 发送失败：回滚 redis
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userKey);
            log.error("Failed to send password reset email to {}", user.getEmail(), ex);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send password reset email");
        }
    }

    private String buildTokenKey(String token) {
        return TOKEN_KEY_PATTERN.formatted(token);
    }

    private String buildUserKey(Long userId) {
        return USER_KEY_PATTERN.formatted(userId);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
