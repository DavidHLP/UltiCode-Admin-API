package com.david.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.david.auth.entity.AuthToken;
import com.david.auth.entity.TokenKind;
import com.david.auth.mapper.AuthTokenMapper;
import com.david.auth.support.JwtToken;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
public class TokenService {

    private final AuthTokenMapper authTokenMapper;
    private final Clock clock;
    private final ZoneId zoneId;

    public TokenService(AuthTokenMapper authTokenMapper, Clock clock) {
        this.authTokenMapper = authTokenMapper;
        this.clock = clock;
        this.zoneId = clock.getZone();
    }

    @Transactional
    public AuthToken storeToken(Long userId, TokenKind kind, JwtToken token) {
        log.info("开始存储用户ID为{}的{}类型令牌", userId, kind);
        revokeTokensByUserAndKind(userId, kind);
        AuthToken entity = new AuthToken();
        entity.setUserId(userId);
        entity.setKind(kind);
        entity.setRevoked(false);
        entity.setToken(hash(token.token()));
        entity.setCreatedAt(LocalDateTime.now(clock));
        entity.setExpiresAt(toLocalDateTime(token.expiresAt()));
        log.debug("存储令牌信息: {}", entity);
        authTokenMapper.insert(entity);
        log.info("成功存储令牌，ID为{}", entity.getId());
        return entity;
    }

    public Optional<AuthToken> findActiveToken(String tokenValue, TokenKind kind) {
        log.debug("查找有效的{}类型令牌", kind);
        LambdaQueryWrapper<AuthToken> query =
                new LambdaQueryWrapper<AuthToken>()
                        .eq(AuthToken::getToken, hash(tokenValue))
                        .eq(AuthToken::getKind, kind)
                        .eq(AuthToken::getRevoked, false);
        AuthToken token = authTokenMapper.selectOne(query);
        if (token == null) {
            log.debug("未找到匹配的令牌");
            return Optional.empty();
        }
        if (token.getExpiresAt() != null
                && token.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
            log.debug(
                    "令牌已过期，令牌ID: {}, 过期时间: {} , 现在时间{}",
                    token.getId(),
                    token.getExpiresAt(),
                    LocalDateTime.now(clock));
            return Optional.empty();
        }
        log.debug("找到有效的令牌，ID为{}", token.getId());
        return Optional.of(token);
    }

    @Transactional
    public void revokeToken(Long tokenId) {
        log.info("撤销ID为{}的令牌", tokenId);
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getId, tokenId)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
        log.info("成功撤销令牌");
    }

    @Transactional
    public void revokeTokensByUserAndKind(Long userId, TokenKind kind) {
        log.info("撤销用户{}的所有{}类型令牌", userId, kind);
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getUserId, userId)
                        .eq(AuthToken::getKind, kind)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
        log.info("成功撤销用户{}的{}类型令牌", userId, kind);
    }

    private String hash(String rawToken) {
        return cn.hutool.crypto.digest.DigestUtil.sha256Hex(rawToken);
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneId);
    }
}
