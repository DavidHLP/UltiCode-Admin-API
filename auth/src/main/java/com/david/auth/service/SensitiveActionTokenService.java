package com.david.auth.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.david.auth.entity.AuthToken;
import com.david.auth.entity.TokenKind;
import com.david.auth.mapper.AuthTokenMapper;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveActionTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RAW_BYTES = 24;
    private static final long DEFAULT_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final AuthTokenMapper authTokenMapper;
    private final Clock clock;

    @Transactional
    public String issueToken(Long userId) {
        revokeExisting(userId);
        String rawToken = generateRawToken();
        AuthToken entity = new AuthToken();
        entity.setUserId(userId);
        entity.setKind(TokenKind.API);
        entity.setRevoked(false);
        entity.setToken(hash(rawToken));
        entity.setCreatedAt(LocalDateTime.now(clock));
        entity.setExpiresAt(
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(clock.millis() + DEFAULT_TTL_MILLIS), clock.getZone()));
        authTokenMapper.insert(entity);
        log.info("颁发敏感操作令牌: tokenId={}, userId={}", entity.getId(), userId);
        return rawToken;
    }

    @Transactional
    public boolean consume(Long userId, String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        LambdaQueryWrapper<AuthToken> query =
                new LambdaQueryWrapper<AuthToken>()
                        .eq(AuthToken::getUserId, userId)
                        .eq(AuthToken::getKind, TokenKind.API)
                        .eq(AuthToken::getToken, hash(rawToken))
                        .eq(AuthToken::getRevoked, false);
        AuthToken token = authTokenMapper.selectOne(query);
        if (token == null) {
            return false;
        }
        if (token.getExpiresAt() != null
                && token.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            log.info("敏感操作令牌已过期: tokenId={}, userId={}", token.getId(), userId);
            revoke(token.getId());
            return false;
        }
        revoke(token.getId());
        return true;
    }

    private void revokeExisting(Long userId) {
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getUserId, userId)
                        .eq(AuthToken::getKind, TokenKind.API)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
    }

    private void revoke(Long tokenId) {
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getId, tokenId)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RAW_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String raw) {
        return DigestUtil.sha256Hex(raw);
    }
}
