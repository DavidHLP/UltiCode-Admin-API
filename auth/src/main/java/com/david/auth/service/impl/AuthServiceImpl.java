package com.david.auth.service.impl;

import com.david.auth.config.AppProperties;
import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.RefreshTokenRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.UserProfileDto;
import com.david.auth.entity.TokenKind;
import com.david.auth.entity.User;
import com.david.auth.exception.BusinessException;
import com.david.auth.security.JwtService;
import com.david.auth.service.AuthService;
import com.david.auth.service.PasswordResetService;
import com.david.auth.service.RegistrationVerificationService;
import com.david.auth.service.TokenService;
import com.david.auth.service.UserService;
import com.david.auth.support.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final RegistrationVerificationService registrationVerificationService;
    private final PasswordResetService passwordResetService;

    public AuthServiceImpl(
            UserService userService,
            TokenService tokenService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties,
            RegistrationVerificationService registrationVerificationService,
            PasswordResetService passwordResetService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
        this.registrationVerificationService = registrationVerificationService;
        this.passwordResetService = passwordResetService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        registrationVerificationService.verifyRegistrationCode(
                request.email(), request.verificationCode());
        User user = userService.register(request);
        List<String> roles = userService.findRoleCodes(user.getId());
        userService.updateLoginMetadata(user.getId(), ipAddress);
        return issueTokens(user, roles);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user =
                userService
                        .findByUsernameOrEmail(request.identifier())
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Invalid username/email or password"));

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account has been disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username/email or password");
        }
        List<String> roles = userService.findRoleCodes(user.getId());
        userService.updateLoginMetadata(user.getId(), ipAddress);
        return issueTokens(user, roles);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        Jws<Claims> parsed = jwtService.parseAndValidate(request.getRefreshToken());
        Claims claims = parsed.getBody();
        if (jwtService.isExpired(claims)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        TokenKind tokenKind = jwtService.extractTokenKind(claims);
        if (!tokenKind.isRefresh()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST, "Only refresh tokens can be used to refresh");
        }
        Long userId = jwtService.extractUserId(claims);
        tokenService
                .findActiveToken(request.getRefreshToken(), TokenKind.REFRESH)
                .orElseThrow(
                        () ->
                                new BusinessException(
                                        HttpStatus.UNAUTHORIZED, "Refresh token is invalid"));

        User user = userService.getActiveUser(userId);
        List<String> roles = userService.findRoleCodes(userId);
        return issueTokens(user, roles);
    }

    @Override
    public UserProfileDto buildUserProfile(Long userId) {
        User user = userService.getActiveUser(userId);
        List<String> roles = userService.findRoleCodes(userId);
        return toProfile(user, roles);
    }

    @Override
    public TokenIntrospectResponse introspectAccessToken(String token) {
        Jws<Claims> parsed = jwtService.parseAndValidate(token);
        Claims claims = parsed.getBody();
        if (jwtService.isExpired(claims)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Access token expired");
        }
        TokenKind tokenKind = jwtService.extractTokenKind(claims);
        if (!tokenKind.isAccess()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Access token required");
        }
        tokenService
                .findActiveToken(token, TokenKind.ACCESS)
                .orElseThrow(
                        () ->
                                new BusinessException(
                                        HttpStatus.UNAUTHORIZED, "Token is invalid or revoked"));
        Long userId = jwtService.extractUserId(claims);
        User user = userService.getActiveUser(userId);
        List<String> roles = userService.findRoleCodes(userId);
        return new TokenIntrospectResponse(user.getId(), user.getUsername(), roles);
    }

    @Override
    public void requestPasswordReset(String email) {
        userService
                .findByUsernameOrEmail(email)
                .ifPresent(
                        user -> {
                            passwordResetService.sendPasswordResetEmail(user);
                        });
    }

    @Override
    public void sendRegistrationVerificationCode(String email) {
        registrationVerificationService.sendRegistrationCode(email);
    }

    private AuthResponse issueTokens(User user, List<String> roles) {
        JwtToken access =
                jwtService.generateToken(user.getId(), user.getUsername(), roles, TokenKind.ACCESS);
        JwtToken refresh =
                jwtService.generateToken(
                        user.getId(), user.getUsername(), roles, TokenKind.REFRESH);

        tokenService.storeToken(user.getId(), TokenKind.ACCESS, access);
        tokenService.storeToken(user.getId(), TokenKind.REFRESH, refresh);

        long accessExpiresIn = appProperties.getSecurity().getJwt().getAccessTokenTtl().toSeconds();
        long refreshExpiresIn =
                appProperties.getSecurity().getJwt().getRefreshTokenTtl().toSeconds();

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(access.token())
                .accessTokenExpiresIn(accessExpiresIn)
                .refreshToken(refresh.token())
                .refreshTokenExpiresIn(refreshExpiresIn)
                .user(toProfile(user, roles))
                .build();
    }

    private UserProfileDto toProfile(User user, List<String> roles) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .build();
    }
}
