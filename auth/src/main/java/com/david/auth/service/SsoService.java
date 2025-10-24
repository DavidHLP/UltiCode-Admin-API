package com.david.auth.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.auth.entity.SsoSession;
import com.david.auth.mapper.SsoSessionMapper;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SsoService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RAW_TOKEN_BYTES = 32;

    private final SsoSessionMapper ssoSessionMapper;
    private final Clock clock;

    @Transactional
    public SsoSession createSession(Long userId, String clientId, String state, long ttlSeconds) {
        String rawToken = generateRawToken();
        String hashedToken = hash(rawToken);
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiresAt = now.plusSeconds(Math.max(ttlSeconds, 60));

        SsoSession session = new SsoSession();
        session.setUserId(userId);
        session.setClientId(clientId);
        session.setSessionToken(hashedToken);
        session.setState(state);
        session.setCreatedAt(now);
        session.setExpiresAt(expiresAt);

        ssoSessionMapper.insert(session);
        session.setSessionToken(rawToken);
        log.info("创建SSO会话: userId={}, clientId={}, expiresAt={}", userId, clientId, expiresAt);
        return session;
    }

    public Optional<SsoSession> validateSession(String rawToken, String clientId) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<SsoSession> query =
                Wrappers.lambdaQuery(SsoSession.class)
                        .eq(SsoSession::getSessionToken, hash(rawToken))
                        .eq(SsoSession::getClientId, clientId)
                        .gt(SsoSession::getExpiresAt, LocalDateTime.now(clock));
        return Optional.ofNullable(ssoSessionMapper.selectOne(query));
    }

    @Transactional
    public void revokeSessions(Long userId, String clientId) {
        LambdaQueryWrapper<SsoSession> query =
                Wrappers.lambdaQuery(SsoSession.class)
                        .eq(SsoSession::getUserId, userId);
        if (clientId != null) {
            query.eq(SsoSession::getClientId, clientId);
        }
        int deleted = ssoSessionMapper.delete(query);
        log.info("撤销SSO会话: userId={}, clientId={}, count={}", userId, clientId, deleted);
    }

    @Transactional
    public void purgeExpired() {
        LambdaQueryWrapper<SsoSession> query =
                Wrappers.lambdaQuery(SsoSession.class)
                        .lt(SsoSession::getExpiresAt, LocalDateTime.now(clock));
        int deleted = ssoSessionMapper.delete(query);
        if (deleted > 0) {
            log.info("清理过期SSO会话数量: {}", deleted);
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String value) {
        return DigestUtil.sha256Hex(value);
    }

    public SsoSession requireValidSession(String rawToken, String clientId) {
        return validateSession(rawToken, clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SSO会话已失效"));
    }

    public List<SsoSession> listSessions(Long userId) {
        LambdaQueryWrapper<SsoSession> query =
                Wrappers.lambdaQuery(SsoSession.class)
                        .eq(SsoSession::getUserId, userId)
                        .orderByDesc(SsoSession::getCreatedAt);
        return ssoSessionMapper.selectList(query);
    }
}
