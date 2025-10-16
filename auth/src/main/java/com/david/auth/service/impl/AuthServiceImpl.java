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

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
        log.info("开始处理用户注册请求，邮箱: {}", request.email());
        registrationVerificationService.verifyRegistrationCode(
                request.email(), request.verificationCode());
        User user = userService.register(request);
        List<String> roles = userService.findRoleCodes(user.getId());
        userService.updateLoginMetadata(user.getId(), ipAddress);
        log.info("用户注册成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        return issueTokens(user, roles);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        log.info("开始处理用户登录请求，标识符: {}", request.identifier());
        User user =
                userService
                        .findByUsernameOrEmail(request.identifier())
                        .orElseThrow(
                                () -> {
                                    log.warn("登录失败，无效的用户名或邮箱: {}", request.identifier());
                                    return new BadCredentialsException(
                                            "Invalid username/email or password");
                                });

        if (user.getStatus() == null || user.getStatus() == 0) {
            log.warn("账户已被禁用，用户ID: {}", user.getId());
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account has been disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("登录失败，密码不匹配，用户ID: {}", user.getId());
            throw new BadCredentialsException("Invalid username/email or password");
        }
        List<String> roles = userService.findRoleCodes(user.getId());
        userService.updateLoginMetadata(user.getId(), ipAddress);
        log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        return issueTokens(user, roles);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.info("开始处理刷新令牌请求");
        Jws<Claims> parsed = jwtService.parseAndValidate(request.getRefreshToken());
        Claims claims = parsed.getBody();
        if (jwtService.isExpired(claims)) {
            log.warn("刷新令牌已过期");
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        TokenKind tokenKind = jwtService.extractTokenKind(claims);
        if (!tokenKind.isRefresh()) {
            log.warn("提供的令牌不是刷新令牌");
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST, "Only refresh tokens can be used to refresh");
        }
        Long userId = jwtService.extractUserId(claims);
        tokenService
                .findActiveToken(request.getRefreshToken(), TokenKind.REFRESH)
                .orElseThrow(
                        () -> {
                            log.warn("刷新令牌无效或已被撤销，用户ID: {}", userId);
                            return new BusinessException(
                                    HttpStatus.UNAUTHORIZED, "Refresh token is invalid");
                        });

        User user = userService.getActiveUser(userId);
        List<String> roles = userService.findRoleCodes(userId);
        log.info("刷新令牌成功，用户ID: {}", userId);
        return issueTokens(user, roles);
    }

    @Override
    public UserProfileDto buildUserProfile(Long userId) {
        log.info("构建用户资料，用户ID: {}", userId);
        User user = userService.getActiveUser(userId);
        List<String> roles = userService.findRoleCodes(userId);
        log.info("用户资料构建完成，用户ID: {}", userId);
        return toProfile(user, roles);
    }

    @Override
    public TokenIntrospectResponse introspectAccessToken(String token) {
        log.info("开始验证访问令牌");
        Jws<Claims> parsed = jwtService.parseAndValidate(token);
        Claims claims = parsed.getBody();
        if (jwtService.isExpired(claims)) {
            log.warn("访问令牌已过期");
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Access token expired");
        }
        TokenKind tokenKind = jwtService.extractTokenKind(claims);
        if (!tokenKind.isAccess()) {
            log.warn("提供的令牌不是访问令牌");
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Access token required");
        }
        tokenService
                .findActiveToken(token, TokenKind.ACCESS)
                .orElseThrow(
                        () -> {
                            log.warn("访问令牌无效或已被撤销");
                            return new BusinessException(
                                    HttpStatus.UNAUTHORIZED, "Token is invalid or revoked");
                        });
        Long userId = jwtService.extractUserId(claims);
        User user = userService.getActiveUser(userId);
        List<String> roles = userService.findRoleCodes(userId);
        log.info("访问令牌验证成功，用户ID: {}", userId);
        return new TokenIntrospectResponse(user.getId(), user.getUsername(), roles);
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("处理密码重置请求，邮箱: {}", email);
        userService
                .findByUsernameOrEmail(email)
                .ifPresentOrElse(
                        user -> {
                            log.info("发送密码重置邮件，用户ID: {}", user.getId());
                            passwordResetService.sendPasswordResetEmail(user);
                        },
                        () -> log.warn("未找到对应邮箱的用户，邮箱: {}", email));
    }

    @Override
    public void sendRegistrationVerificationCode(String email) {
        log.info("发送注册验证码，邮箱: {}", email);
        registrationVerificationService.sendRegistrationCode(email);
        log.info("注册验证码已发送，邮箱: {}", email);
    }

    private AuthResponse issueTokens(User user, List<String> roles) {
        log.debug("为用户生成访问和刷新令牌，用户ID: {}", user.getId());
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

        log.info(
                "令牌颁发完成，用户ID: {}, 访问令牌过期时间: {}秒, 刷新令牌过期时间: {}秒",
                user.getId(),
                accessExpiresIn,
                refreshExpiresIn);
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
        log.debug("转换用户资料DTO，用户ID: {}", user.getId());
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
