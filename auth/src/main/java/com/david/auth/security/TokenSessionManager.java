package com.david.auth.security;

import com.david.auth.config.AppProperties;
import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.RefreshTokenRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenSessionManager {

    private static final String SESSION_ACCESS_TOKEN = "CF_SESSION_ACCESS_TOKEN";
    private static final String SESSION_REFRESH_TOKEN = "CF_SESSION_REFRESH_TOKEN";
    private static final String SESSION_ACCESS_EXPIRES_AT = "CF_SESSION_ACCESS_EXPIRES_AT";
    private static final String SESSION_REFRESH_EXPIRES_AT = "CF_SESSION_REFRESH_EXPIRES_AT";

    private final AppProperties appProperties;

    public void storeAuthResult(
            HttpServletRequest request, HttpServletResponse response, AuthResponse authResponse) {
        if (authResponse == null) {
            clear(request, response);
            return;
        }
        HttpSession session = request.getSession(true);
        if (StringUtils.hasText(authResponse.accessToken())) {
            session.setAttribute(SESSION_ACCESS_TOKEN, authResponse.accessToken());
            session.setAttribute(
                    SESSION_ACCESS_EXPIRES_AT,
                    System.currentTimeMillis() + authResponse.accessTokenExpiresIn() * 1000);
        } else {
            session.removeAttribute(SESSION_ACCESS_TOKEN);
            session.removeAttribute(SESSION_ACCESS_EXPIRES_AT);
        }
        if (StringUtils.hasText(authResponse.refreshToken())) {
            session.setAttribute(SESSION_REFRESH_TOKEN, authResponse.refreshToken());
            session.setAttribute(
                    SESSION_REFRESH_EXPIRES_AT,
                    System.currentTimeMillis() + authResponse.refreshTokenExpiresIn() * 1000);
            writeRefreshCookie(response, authResponse.refreshToken(), authResponse.refreshTokenExpiresIn());
        } else {
            clearRefreshCookie(response);
            session.removeAttribute(SESSION_REFRESH_TOKEN);
            session.removeAttribute(SESSION_REFRESH_EXPIRES_AT);
        }
    }

    public void clear(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_ACCESS_TOKEN);
            session.removeAttribute(SESSION_REFRESH_TOKEN);
            session.removeAttribute(SESSION_ACCESS_EXPIRES_AT);
            session.removeAttribute(SESSION_REFRESH_EXPIRES_AT);
        }
        clearRefreshCookie(response);
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object attribute = session.getAttribute(SESSION_ACCESS_TOKEN);
            if (attribute instanceof String token && StringUtils.hasText(token)) {
                Object expiresAt = session.getAttribute(SESSION_ACCESS_EXPIRES_AT);
                if (isExpired(expiresAt)) {
                    session.removeAttribute(SESSION_ACCESS_TOKEN);
                    session.removeAttribute(SESSION_ACCESS_EXPIRES_AT);
                    return null;
                }
                return token;
            }
        }
        return null;
    }

    public String resolveRefreshToken(
            HttpServletRequest request, RefreshTokenRequest refreshTokenRequest) {
        if (refreshTokenRequest != null
                && StringUtils.hasText(refreshTokenRequest.refreshToken())) {
            return refreshTokenRequest.refreshToken();
        }
        String refreshFromCookie = readRefreshCookie(request);
        if (StringUtils.hasText(refreshFromCookie)) {
            return refreshFromCookie;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object attribute = session.getAttribute(SESSION_REFRESH_TOKEN);
            if (attribute instanceof String token && StringUtils.hasText(token)) {
                Object expiresAt = session.getAttribute(SESSION_REFRESH_EXPIRES_AT);
                if (isExpired(expiresAt)) {
                    session.removeAttribute(SESSION_REFRESH_TOKEN);
                    session.removeAttribute(SESSION_REFRESH_EXPIRES_AT);
                    return null;
                }
                return token;
            }
        }
        return null;
    }

    private void writeRefreshCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        AppProperties.Security.Cookies cookies = appProperties.getSecurity().getCookies();
        ResponseCookie.ResponseCookieBuilder builder =
                ResponseCookie.from(cookies.getRefreshTokenName(), token)
                        .httpOnly(true)
                        .secure(cookies.isSecure())
                        .sameSite(cookies.getSameSite())
                        .path(cookies.getPath())
                        .maxAge(Duration.ofSeconds(Math.max(maxAgeSeconds, 0)));
        if (StringUtils.hasText(cookies.getDomain())) {
            builder.domain(cookies.getDomain());
        }
        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("已写入刷新令牌Cookie，名称: {}", cookies.getRefreshTokenName());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        AppProperties.Security.Cookies cookies = appProperties.getSecurity().getCookies();
        ResponseCookie.ResponseCookieBuilder builder =
                ResponseCookie.from(cookies.getRefreshTokenName(), "")
                        .httpOnly(true)
                        .secure(cookies.isSecure())
                        .sameSite(cookies.getSameSite())
                        .path(cookies.getPath())
                        .maxAge(Duration.ZERO);
        if (StringUtils.hasText(cookies.getDomain())) {
            builder.domain(cookies.getDomain());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        String cookieName = appProperties.getSecurity().getCookies().getRefreshTokenName();
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookieName, cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isExpired(Object value) {
        if (value instanceof Number number) {
            return number.longValue() <= System.currentTimeMillis();
        }
        return false;
    }
}
