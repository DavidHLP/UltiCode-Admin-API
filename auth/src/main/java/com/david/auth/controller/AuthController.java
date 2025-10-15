package com.david.auth.controller;

import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.PasswordResetRequest;
import com.david.auth.dto.RefreshTokenRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.RegistrationCodeRequest;
import com.david.auth.dto.TokenIntrospectRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.UserProfileDto;
import com.david.auth.security.UserPrincipal;
import com.david.auth.service.AuthService;
import com.david.common.http.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        log.debug("Register request received for email: {}", request.email());
        AuthResponse response = authService.register(request, resolveClientIp(httpRequest));
        log.debug("Registration completed successfully for email: {}", request.email());
        return ApiResponse.success(response);
    }

    @PostMapping("/register/code")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> sendRegistrationCode(
            @Valid @RequestBody RegistrationCodeRequest request) {
        log.debug("Registration code request received for email: {}", request.email());
        authService.sendRegistrationVerificationCode(request.email());
        log.debug("Registration code sent successfully for email: {}", request.email());
        return ApiResponse.success(null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.debug("Login request received for email or username : {}", request.identifier());
        AuthResponse response = authService.login(request, resolveClientIp(httpRequest));
        log.debug("Login completed successfully for email or username : {}", request.identifier());
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");
        AuthResponse response = authService.refresh(request);
        log.debug("Token refresh completed successfully");
        return ApiResponse.success(response);
    }

    @PostMapping("/introspect")
    public ApiResponse<TokenIntrospectResponse> introspect(
            @Valid @RequestBody TokenIntrospectRequest request) {
        log.debug("Token introspection request received");
        TokenIntrospectResponse response = authService.introspectAccessToken(request.token());
        log.debug("Token introspection completed");
        return ApiResponse.success(response);
    }

    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> forgot(@Valid @RequestBody PasswordResetRequest request) {
        log.debug("Password reset request received for email: {}", request.email());
        authService.requestPasswordReset(request.email());
        log.debug("Password reset request processed for email: {}", request.email());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("User profile request received for user id: {}", principal.id());
        UserProfileDto profile = authService.buildUserProfile(principal.id());
        log.debug("User profile retrieved for user id: {}", principal.id());
        return ApiResponse.success(profile);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
