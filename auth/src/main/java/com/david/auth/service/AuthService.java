package com.david.auth.service;

import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.UserProfileDto;
import com.david.auth.dto.TwoFactorSetupResponse;
import com.david.auth.dto.SsoSessionResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request, String ipAddress);

    AuthResponse login(LoginRequest request, String ipAddress);

    AuthResponse refresh(String refreshToken);

    UserProfileDto buildUserProfile(Long userId);

    TokenIntrospectResponse introspectAccessToken(String token);

    void requestPasswordReset(String email);

    void sendRegistrationVerificationCode(String email);

    TwoFactorSetupResponse enableTwoFactor(Long userId);

    void disableTwoFactor(Long userId);

    SsoSessionResponse initiateSso(Long userId, String clientId, String state, long ttlSeconds);

    void revokeSsoSessions(Long userId, String clientId);

    void sendSensitiveActionCode(Long userId);

    String issueSensitiveActionToken(Long userId, String verificationCode);

    boolean verifySensitiveActionToken(Long userId, String token);
}
