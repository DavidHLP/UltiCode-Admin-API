package com.david.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.david.auth.entity.AuthToken;
import com.david.auth.entity.TokenKind;
import com.david.auth.mapper.AuthTokenMapper;
import com.david.auth.support.JwtToken;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class TokenService {

    private final AuthTokenMapper authTokenMapper;

    public TokenService(AuthTokenMapper authTokenMapper) {
        this.authTokenMapper = authTokenMapper;
    }

    @Transactional
    public AuthToken storeToken(Long userId, TokenKind kind, JwtToken token) {
        revokeTokensByUserAndKind(userId, kind);
        AuthToken entity = new AuthToken();
        entity.setUserId(userId);
        entity.setKind(kind);
        entity.setRevoked(false);
        entity.setToken(hash(token.token()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(toLocalDateTime(token.expiresAt()));
        authTokenMapper.insert(entity);
        return entity;
    }

    public Optional<AuthToken> findActiveToken(String tokenValue, TokenKind kind) {
        LambdaQueryWrapper<AuthToken> query =
                new LambdaQueryWrapper<AuthToken>()
                        .eq(AuthToken::getToken, hash(tokenValue))
                        .eq(AuthToken::getKind, kind)
                        .eq(AuthToken::getRevoked, false);
        AuthToken token = authTokenMapper.selectOne(query);
        if (token == null) {
            return Optional.empty();
        }
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(token);
    }

    @Transactional
    public void revokeToken(Long tokenId) {
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getId, tokenId)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
    }

    @Transactional
    public void revokeTokensByUserAndKind(Long userId, TokenKind kind) {
        LambdaUpdateWrapper<AuthToken> update =
                new LambdaUpdateWrapper<AuthToken>()
                        .eq(AuthToken::getUserId, userId)
                        .eq(AuthToken::getKind, kind)
                        .set(AuthToken::getRevoked, true);
        authTokenMapper.update(null, update);
    }

    private String hash(String rawToken) {
        return cn.hutool.crypto.digest.DigestUtil.sha256Hex(rawToken);
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
