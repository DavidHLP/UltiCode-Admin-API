package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.david.admin.entity.AuthToken;
import com.david.admin.mapper.AuthTokenMapper;
import java.time.Clock;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveActionValidator {

    private final AuthTokenMapper authTokenMapper;
    private final Clock clock;

    public boolean verifyAndConsume(Long userId, String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        LambdaQueryWrapper<AuthToken> query =
                new LambdaQueryWrapper<AuthToken>()
                        .eq(AuthToken::getUserId, userId)
                        .eq(AuthToken::getKind, "api")
                        .eq(AuthToken::getToken, hash(rawToken))
                        .eq(AuthToken::getRevoked, false);
        AuthToken token = authTokenMapper.selectOne(query);
        if (token == null) {
            return false;
        }
        if (token.getExpiresAt() != null
                && token.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            log.info("敏感操作令牌已过期，tokenId={}、userId={}", token.getId(), userId);
            revoke(token.getId());
            return false;
        }
        revoke(token.getId());
        return true;
    }

    private void revoke(Long tokenId) {
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getId, tokenId)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("缺少SHA-256算法", ex);
        }
    }
}
