package com.david.auth.controller;

import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.PasswordResetRequest;
import com.david.auth.dto.RefreshTokenRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.RegistrationCodeRequest;
import com.david.auth.dto.SensitiveActionTokenRequest;
import com.david.auth.dto.SensitiveActionTokenVerifyRequest;
import com.david.auth.dto.SsoInitiateRequest;
import com.david.auth.dto.SsoRevokeRequest;
import com.david.auth.dto.SsoSessionResponse;
import com.david.auth.dto.TokenIntrospectRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.TwoFactorSetupResponse;
import com.david.auth.dto.UserProfileDto;
import com.david.auth.exception.BusinessException;
import com.david.auth.security.TokenSessionManager;
import com.david.auth.security.UserPrincipal;
import com.david.auth.service.AuthService;
import com.david.common.http.ApiResponse;
import com.david.common.security.SensitiveDataMasker;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenSessionManager tokenSessionManager;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String maskedEmail = SensitiveDataMasker.maskEmail(request.email());
        log.debug("收到邮箱为 {} 的注册请求", maskedEmail);
        AuthResponse response = authService.register(request, resolveClientIp(httpRequest));
        tokenSessionManager.storeAuthResult(httpRequest, httpResponse, response);
        log.debug("邮箱为 {} 的注册请求处理成功", maskedEmail);
        return ApiResponse.success(response);
    }

    @PostMapping("/register/code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> sendRegistrationCode(
            @Valid @RequestBody RegistrationCodeRequest request) {
        log.debug("收到邮箱为 {} 的注册验证码请求", request.email());
        authService.sendRegistrationVerificationCode(request.email());
        log.debug("已成功向邮箱 {} 发送注册验证码", request.email());
        return ApiResponse.success(null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String maskedIdentifier = maskIdentifier(request.identifier());
        log.debug("收到邮箱或用户名为 {} 的登录请求", maskedIdentifier);
        AuthResponse response = authService.login(request, resolveClientIp(httpRequest));
        tokenSessionManager.storeAuthResult(httpRequest, httpResponse, response);
        log.debug("邮箱或用户名为 {} 的登录请求处理成功", maskedIdentifier);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.debug("收到令牌刷新请求");
        String refreshToken = tokenSessionManager.resolveRefreshToken(httpRequest, request);
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "缺少刷新令牌");
        }
        AuthResponse response = authService.refresh(refreshToken);
        tokenSessionManager.storeAuthResult(httpRequest, httpResponse, response);
        log.debug("令牌刷新请求处理成功");
        return ApiResponse.success(response);
    }

    @PostMapping("/introspect")
    public ApiResponse<TokenIntrospectResponse> introspect(
            @Valid @RequestBody TokenIntrospectRequest request) {
        log.debug("收到令牌验证请求");
        TokenIntrospectResponse response = authService.introspectAccessToken(request.token());
        log.debug("令牌验证请求处理完成 response{}", response);
        return ApiResponse.success(response);
    }

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> forgot(@Valid @RequestBody PasswordResetRequest request) {
        String maskedEmail = SensitiveDataMasker.maskEmail(request.email());
        log.debug("收到邮箱为 {} 的密码重置请求", maskedEmail);
        authService.requestPasswordReset(request.email());
        log.debug("邮箱为 {} 的密码重置请求已处理", maskedEmail);
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("收到用户ID为 {} 的个人资料请求", principal.id());
        UserProfileDto profile = authService.buildUserProfile(principal.id());
        log.debug("已成功获取用户ID为 {} 的个人资料", principal.id());
        return ApiResponse.success(profile);
    }

    @PostMapping("/mfa/enable")
    public ApiResponse<TwoFactorSetupResponse> enableMfa(
            @AuthenticationPrincipal UserPrincipal principal) {
        log.debug("用户 {} 请求启用二次验证", principal.id());
        TwoFactorSetupResponse response = authService.enableTwoFactor(principal.id());
        return ApiResponse.success(response);
    }

    @PostMapping("/mfa/disable")
    public ApiResponse<Void> disableMfa(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("用户 {} 请求关闭二次验证", principal.id());
        authService.disableTwoFactor(principal.id());
        return ApiResponse.success(null);
    }

    @PostMapping("/sso/initiate")
    public ApiResponse<SsoSessionResponse> initiateSso(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SsoInitiateRequest request) {
        log.debug("用户 {} 请求创建SSO会话，clientId={}", principal.id(), request.clientId());
        long ttl = request.ttlSeconds() == null ? 300 : request.ttlSeconds();
        SsoSessionResponse response =
                authService.initiateSso(principal.id(), request.clientId(), request.state(), ttl);
        return ApiResponse.success(response);
    }

    @PostMapping("/sso/revoke")
    public ApiResponse<Void> revokeSso(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) SsoRevokeRequest request) {
        String clientId = request == null ? null : request.clientId();
        log.debug("用户 {} 请求撤销SSO会话，clientId={}", principal.id(), clientId);
        authService.revokeSsoSessions(principal.id(), clientId);
        return ApiResponse.success(null);
    }

    @PostMapping("/sensitive-token")
    public ApiResponse<String> issueSensitiveToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SensitiveActionTokenRequest request) {
        log.debug("用户 {} 请求生成敏感操作令牌", principal.id());
        String token =
                authService.issueSensitiveActionToken(principal.id(), request.twoFactorCode());
        return ApiResponse.success(token);
    }

    @PostMapping("/sensitive-token/verify")
    public ApiResponse<Boolean> verifySensitiveToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SensitiveActionTokenVerifyRequest request) {
        boolean result = authService.verifySensitiveActionToken(principal.id(), request.token());
        return ApiResponse.success(result);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        if (identifier.contains("@")) {
            return SensitiveDataMasker.maskEmail(identifier);
        }
        if (identifier.length() <= 2) {
            return "***";
        }
        return identifier.substring(0, 2) + "***";
    }
}
