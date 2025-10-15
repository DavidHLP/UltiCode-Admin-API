package com.david.auth.service.impl;

import com.david.auth.config.AppProperties;
import com.david.auth.entity.User;
import com.david.auth.exception.BusinessException;
import com.david.auth.service.PasswordResetService;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

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
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String TOKEN_KEY_PATTERN = "auth:password-reset:token:%s";
    private static final String USER_KEY_PATTERN = "auth:password-reset:user:%d";

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final String defaultFromAddress;

    public PasswordResetServiceImpl(
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

        redisTemplate.opsForValue().set(tokenKey, user.getId().toString(), ttl);
        redisTemplate.opsForValue().set(userKey, token, ttl);

        try {
            sendEmail(user.getEmail(), token, ttl);
            log.debug("Sent password reset email to {}", user.getEmail());
        } catch (MailException | MessagingException ex) {
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(userKey);
            log.error("Failed to send password reset email to {}", user.getEmail(), ex);
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send password reset email");
        }
    }

    private void sendEmail(String email, String token, Duration ttl) throws MessagingException {
        String urlTemplate = appProperties.getMail().getPasswordResetUrlTemplate();
        String resetUrl = String.format(urlTemplate, token);
        long ttlMinutes = Math.max(1, ttl.toMinutes());
        String template = appProperties.getMail().getPasswordResetTemplate();
        String htmlContent = String.format(template, resetUrl, ttlMinutes);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        if (StringUtils.hasText(defaultFromAddress)) {
            helper.setFrom(defaultFromAddress);
        }
        helper.setTo(email);
        helper.setSubject(appProperties.getMail().getPasswordResetSubject());
        helper.setText(htmlContent, true);
        mailSender.send(message);
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
