package com.david.auth.service;

import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.UserProfileDto;

public interface AuthService {

    AuthResponse register(RegisterRequest request, String ipAddress);

    AuthResponse login(LoginRequest request, String ipAddress);

    AuthResponse refresh(String refreshToken);

    UserProfileDto buildUserProfile(Long userId);

    TokenIntrospectResponse introspectAccessToken(String token);

    void requestPasswordReset(String email);

    void sendRegistrationVerificationCode(String email);
}
