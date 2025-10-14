package com.david.auth.service;

import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.RefreshTokenRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.UserProfileDto;
import com.david.auth.entity.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request, String ipAddress);

    AuthResponse login(LoginRequest request, String ipAddress);

    AuthResponse refresh(RefreshTokenRequest request);

    UserProfileDto buildUserProfile(Long userId);

    TokenIntrospectResponse introspectAccessToken(String token);

    void requestPasswordReset(String email);
}
