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
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtService {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_TOKEN_TYPE = "token_type";

    private final AppProperties appProperties;
    private final Key signingKey;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.signingKey = buildKey(appProperties.getSecurity().getJwt().getSecret());
    }

    private Key buildKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtToken generateToken(Long userId,
                                  String username,
                                  List<String> roles,
                                  TokenKind kind) {
        AppProperties.Security.Jwt jwtProps = appProperties.getSecurity().getJwt();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(kind.isAccess() ? jwtProps.getAccessTokenTtl() : jwtProps.getRefreshTokenTtl());

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(jwtProps.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .addClaims(Map.of(
                        CLAIM_USERNAME, username,
                        CLAIM_ROLES, roles,
                        CLAIM_TOKEN_TYPE, kind.getValue()
                ))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, expiresAt);
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(appProperties.getSecurity().getJwt().getIssuer())
                .build()
                .parseClaimsJws(token);
    }

    public TokenKind extractTokenKind(Claims claims) {
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        for (TokenKind kind : TokenKind.values()) {
            if (kind.getValue().equalsIgnoreCase(tokenType)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown token type: " + tokenType);
    }

    public List<String> extractRoles(Claims claims) {
        return claims.get(CLAIM_ROLES, List.class);
    }

    public Long extractUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String extractUsername(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    public boolean isExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration == null || expiration.toInstant().isBefore(Instant.now().minus(1, ChronoUnit.SECONDS));
    }
}
