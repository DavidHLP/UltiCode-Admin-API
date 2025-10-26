package com.david.auth.service.impl;

import com.david.auth.config.AppProperties;
import com.david.auth.dto.AuthResponse;
import com.david.auth.dto.LoginRequest;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.dto.TokenIntrospectResponse;
import com.david.auth.dto.UserProfileDto;
import com.david.auth.dto.SsoSessionResponse;
import com.david.auth.dto.TwoFactorSetupResponse;
import com.david.auth.entity.TokenKind;
import com.david.auth.entity.SsoSession;
import com.david.auth.entity.User;
import com.david.common.http.exception.BusinessException;
import com.david.auth.security.JwtService;
import com.david.auth.service.AuthService;
import com.david.auth.service.AuditLogService;
import com.david.auth.service.CaptchaService;
import com.david.auth.service.PasswordResetService;
import com.david.auth.service.RegistrationVerificationService;
import com.david.auth.service.SecurityPolicyService;
import com.david.auth.service.SensitiveActionTokenService;
import com.david.auth.service.SensitiveActionVerificationService;
import com.david.auth.service.SsoService;
import com.david.auth.service.TokenService;
import com.david.auth.service.UserService;
import com.david.auth.service.TwoFactorService;
import com.david.auth.support.JwtToken;
import com.david.auth.entity.UserSecurityProfile;
import com.david.common.security.AuditAction;
import com.david.common.security.SecurityAuditRecord;
import com.david.common.security.SensitiveDataMasker;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final TwoFactorService twoFactorService;
    private final AuditLogService auditLogService;
    private final SecurityPolicyService securityPolicyService;
    private final CaptchaService captchaService;
    private final SsoService ssoService;
    private final SensitiveActionTokenService sensitiveActionTokenService;
    private final SensitiveActionVerificationService sensitiveActionVerificationService;

    public AuthServiceImpl(
            UserService userService,
            TokenService tokenService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties,
            RegistrationVerificationService registrationVerificationService,
            PasswordResetService passwordResetService,
            TwoFactorService twoFactorService,
            AuditLogService auditLogService,
            SecurityPolicyService securityPolicyService,
            CaptchaService captchaService,
            SsoService ssoService,
            SensitiveActionTokenService sensitiveActionTokenService,
            SensitiveActionVerificationService sensitiveActionVerificationService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
        this.registrationVerificationService = registrationVerificationService;
        this.passwordResetService = passwordResetService;
        this.twoFactorService = twoFactorService;
        this.auditLogService = auditLogService;
        this.securityPolicyService = securityPolicyService;
        this.captchaService = captchaService;
        this.ssoService = ssoService;
        this.sensitiveActionTokenService = sensitiveActionTokenService;
        this.sensitiveActionVerificationService = sensitiveActionVerificationService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        String maskedEmail = SensitiveDataMasker.maskEmail(request.email());
        log.info("开始处理用户注册请求，邮箱: {}", maskedEmail);
        captchaService.verify(request.captchaToken());
        securityPolicyService.ensureIpAllowed(ipAddress);
        registrationVerificationService.verifyRegistrationCode(
                request.email(), request.verificationCode());
        User user = userService.register(request);
        List<String> roles = userService.findRoleCodes(user.getId());
        userService.updateLoginMetadata(user.getId(), ipAddress);
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(user.getId())
                        .actorUsername(user.getUsername())
                        .action(AuditAction.USER_REGISTERED)
                        .objectType("user")
                        .objectId(String.valueOf(user.getId()))
                        .description("用户完成自助注册")
                        .ipAddress(ipAddress)
                        .build());
        log.info("用户注册成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        return issueTokens(user, roles);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String maskedIdentifier = maskIdentifier(request.identifier());
        log.info("开始处理用户登录请求，标识符: {}", maskedIdentifier);
        securityPolicyService.ensureIpAllowed(ipAddress);
        securityPolicyService.registerLoginAttempt(ipAddress);
        captchaService.verify(request.captchaToken());

        User user =
                userService.findByUsernameOrEmail(request.identifier()).orElse(null);

        if (user == null) {
            log.warn("登录失败，账号不存在: {}", maskedIdentifier);
            auditLogService.record(
                    SecurityAuditRecord.builder()
                            .action(AuditAction.LOGIN_FAILURE)
                            .objectType("user")
                            .description("账号不存在或密码错误")
                            .ipAddress(ipAddress)
                            .build());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            log.warn("账户已被禁用，用户ID: {}", user.getId());
            auditLogService.record(
                    SecurityAuditRecord.builder()
                            .actorId(user.getId())
                            .actorUsername(user.getUsername())
                            .action(AuditAction.LOGIN_FAILURE)
                            .objectType("user")
                            .objectId(String.valueOf(user.getId()))
                            .description("账户已被禁用")
                            .ipAddress(ipAddress)
                            .build());
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account has been disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("登录失败，密码不匹配，用户ID: {}", user.getId());
            auditLogService.record(
                    SecurityAuditRecord.builder()
                            .actorId(user.getId())
                            .actorUsername(user.getUsername())
                            .action(AuditAction.LOGIN_FAILURE)
                            .objectType("user")
                            .objectId(String.valueOf(user.getId()))
                            .description("密码校验失败")
                            .ipAddress(ipAddress)
                            .build());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        UserSecurityProfile profile =
                twoFactorService.findProfile(user.getId()).orElse(null);
        if (profile != null && Boolean.TRUE.equals(profile.getMfaEnabled())) {
            if (!StringUtils.hasText(request.twoFactorCode())
                    || !twoFactorService.verifyCode(profile.getMfaSecret(), request.twoFactorCode())) {
                log.warn("登录失败，二次验证未通过，用户ID: {}", user.getId());
                auditLogService.record(
                        SecurityAuditRecord.builder()
                                .actorId(user.getId())
                                .actorUsername(user.getUsername())
                                .action(AuditAction.LOGIN_FAILURE)
                                .objectType("user")
                                .objectId(String.valueOf(user.getId()))
                                .description("二次验证失败")
                                .ipAddress(ipAddress)
                                .build());
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "需要有效的二次验证码");
            }
            twoFactorService.markVerified(user.getId());
        }

        List<String> roles = userService.findRoleCodes(user.getId());
        userService.updateLoginMetadata(user.getId(), ipAddress);
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(user.getId())
                        .actorUsername(user.getUsername())
                        .action(AuditAction.LOGIN_SUCCESS)
                        .objectType("user")
                        .objectId(String.valueOf(user.getId()))
                        .description("登录成功")
                        .ipAddress(ipAddress)
                        .build());
        log.info("用户登录成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        return issueTokens(user, roles);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        log.info("开始处理刷新令牌请求");
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
        Jws<Claims> parsed = jwtService.parseAndValidate(refreshToken);
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
                .findActiveToken(refreshToken, TokenKind.REFRESH)
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
        String maskedEmail = SensitiveDataMasker.maskEmail(email);
        log.info("处理密码重置请求，邮箱: {}", maskedEmail);
        userService
                .findByUsernameOrEmail(email)
                .ifPresentOrElse(
                        user -> {
                            log.info("发送密码重置邮件，用户ID: {}", user.getId());
                            passwordResetService.sendPasswordResetEmail(user);
                        },
                        () -> log.warn("未找到对应邮箱的用户，邮箱: {}", maskedEmail));
    }

    @Override
    public void sendRegistrationVerificationCode(String email) {
        String maskedEmail = SensitiveDataMasker.maskEmail(email);
        log.info("发送注册验证码，邮箱: {}", maskedEmail);
        registrationVerificationService.sendRegistrationCode(email);
        log.info("注册验证码已发送，邮箱: {}", maskedEmail);
    }

    @Override
    @Transactional
    public TwoFactorSetupResponse enableTwoFactor(Long userId) {
        User user = userService.getActiveUser(userId);
        UserSecurityProfile profile = twoFactorService.enableMfa(userId);
        String provisioningUri =
                twoFactorService.generateProvisioningUri(
                        appProperties.getSecurity().getJwt().getIssuer(),
                        user.getUsername(),
                        profile.getMfaSecret());
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(user.getId())
                        .actorUsername(user.getUsername())
                        .action(AuditAction.TWO_FACTOR_ENABLED)
                        .objectType("user_security")
                        .objectId(String.valueOf(userId))
                        .description("启用二次验证")
                        .build());
        return TwoFactorSetupResponse.builder()
                .enabled(Boolean.TRUE.equals(profile.getMfaEnabled()))
                .secret(profile.getMfaSecret())
                .provisioningUri(provisioningUri)
                .userId(userId)
                .build();
    }

    @Override
    @Transactional
    public void disableTwoFactor(Long userId) {
        User user = userService.getActiveUser(userId);
        twoFactorService.disableMfa(userId);
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(user.getId())
                        .actorUsername(user.getUsername())
                        .action(AuditAction.TWO_FACTOR_DISABLED)
                        .objectType("user_security")
                        .objectId(String.valueOf(userId))
                        .description("关闭二次验证")
                        .build());
    }

    @Override
    @Transactional
    public SsoSessionResponse initiateSso(Long userId, String clientId, String state, long ttlSeconds) {
        User user = userService.getActiveUser(userId);
        SsoSession session = ssoService.createSession(userId, clientId, state, ttlSeconds);
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(user.getId())
                        .actorUsername(user.getUsername())
                        .action(AuditAction.SSO_SESSION_CREATED)
                        .objectType("sso_session")
                        .objectId(String.valueOf(session.getId()))
                        .description("创建SSO会话")
                        .build());
        return SsoSessionResponse.builder()
                .userId(userId)
                .clientId(clientId)
                .state(state)
                .token(session.getSessionToken())
                .expiresAt(session.getExpiresAt())
                .build();
    }

    @Override
    @Transactional
    public void revokeSsoSessions(Long userId, String clientId) {
        User user = userService.getActiveUser(userId);
        ssoService.revokeSessions(userId, clientId);
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(user.getId())
                        .actorUsername(user.getUsername())
                        .action(AuditAction.SSO_SESSION_REVOKED)
                        .objectType("sso_session")
                        .objectId(clientId == null ? "ALL" : clientId)
                        .description("撤销SSO会话")
                        .build());
    }

    @Override
    public void sendSensitiveActionCode(Long userId) {
        log.debug("用户 {} 请求发送敏感操作验证码", userId);
        User user = userService.getActiveUser(userId);
        sensitiveActionVerificationService.sendVerificationCode(user);
    }

    @Override
    public String issueSensitiveActionToken(Long userId, String verificationCode) {
        User user = userService.getActiveUser(userId);
        sensitiveActionVerificationService.verifyCode(userId, verificationCode);
        String token = sensitiveActionTokenService.issueToken(userId);
        auditLogService.record(
                SecurityAuditRecord.builder()
                        .actorId(userId)
                        .actorUsername(user.getUsername())
                        .action(AuditAction.SENSITIVE_ACTION_TOKEN_ISSUED)
                        .objectType("sensitive_token")
                        .objectId("API")
                        .description("生成敏感操作二次校验令牌")
                        .build());
        return token;
    }

    @Override
    public boolean verifySensitiveActionToken(Long userId, String token) {
        return sensitiveActionTokenService.consume(userId, token);
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

    private String maskIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return identifier;
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
