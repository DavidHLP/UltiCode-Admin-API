package com.david.auth.security;

import com.david.auth.config.AppProperties;
import com.david.auth.entity.TokenKind;
import com.david.auth.support.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtService {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TOKEN_TYPE = "token_type";

    private final AppProperties appProperties;
    private final Key signingKey;
    private final Clock clock;

    public JwtService(AppProperties appProperties, Clock clock) {
        this.appProperties = appProperties;
        this.clock = clock;
        this.signingKey = buildKey(appProperties.getSecurity().getJwt().getSecret());
        log.info("JWT服务初始化完成，签名密钥已构建");
    }

    private Key buildKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            log.debug("使用Base64解码的密钥构建签名密钥");
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            log.debug("使用UTF-8编码的密钥构建签名密钥");
        }
        log.trace("签名密钥构建完成");
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtToken generateToken(
            Long userId, String username, List<String> roles, TokenKind kind) {
        log.info("开始生成{}令牌，用户ID: {}, 用户名: {}", kind.getValue(), userId, username);
        AppProperties.Security.Jwt jwtProps = appProperties.getSecurity().getJwt();
        Instant now = Instant.now(clock);
        Instant expiresAt =
                now.plus(
                        kind.isAccess()
                                ? jwtProps.getAccessTokenTtl()
                                : jwtProps.getRefreshTokenTtl());
        log.debug("{}令牌有效期至: {}", kind.getValue(), expiresAt);

        String token =
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .setIssuer(jwtProps.getIssuer())
                        .setIssuedAt(Date.from(now))
                        .setExpiration(Date.from(expiresAt))
                        .addClaims(
                                Map.of(
                                        CLAIM_USERNAME,
                                        username,
                                        CLAIM_ROLES,
                                        roles,
                                        CLAIM_TOKEN_TYPE,
                                        kind.getValue()))
                        .signWith(signingKey, SignatureAlgorithm.HS256)
                        .compact();
        log.info("{}令牌生成成功", kind.getValue());
        return new JwtToken(token, expiresAt);
    }

    public Jws<Claims> parseAndValidate(String token) {
        log.trace("开始解析和验证JWT令牌");
        Jws<Claims> claims =
                Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .requireIssuer(appProperties.getSecurity().getJwt().getIssuer())
                        .build()
                        .parseClaimsJws(token);
        log.debug("JWT令牌解析验证成功，主题: {}", claims.getBody().getSubject());
        return claims;
    }

    public TokenKind extractTokenKind(Claims claims) {
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        log.trace("提取令牌类型: {}", tokenType);
        for (TokenKind kind : TokenKind.values()) {
            if (kind.getValue().equalsIgnoreCase(tokenType)) {
                log.debug("识别到有效的令牌类型: {}", kind.getValue());
                return kind;
            }
        }
        log.warn("发现未知的令牌类型: {}", tokenType);
        throw new IllegalArgumentException("Unknown token type: " + tokenType);
    }

    public List<String> extractRoles(Claims claims) {
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        log.trace("提取用户角色列表: {}", roles);
        return roles;
    }

    public Long extractUserId(Claims claims) {
        String subject = claims.getSubject();
        log.trace("提取用户ID: {}", subject);
        return Long.valueOf(subject);
    }

    public String extractUsername(Claims claims) {
        String username = claims.get(CLAIM_USERNAME, String.class);
        log.trace("提取用户名: {}", username);
        return username;
    }

    public boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        Instant now = Instant.now(clock);
        boolean expired =
                expiration == null
                        || expiration.toInstant().isBefore(now.minus(1, ChronoUnit.SECONDS));
        log.debug("检查令牌是否过期: {}", expired);
        return expired;
    }
}
