package com.david.service.imp;

import com.david.entity.token.Token;
import com.david.entity.token.TokenType;
import com.david.entity.user.AuthUser;
import com.david.exception.BizException;
import com.david.mapper.TokenMapper;
import com.david.mapper.UserMapper;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.core.lock.DistributedLockManager;
import com.david.service.AuthService;
import com.david.service.EmailService;
import com.david.utils.JwtService;
import com.david.utils.enums.ResponseCode;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    // Redis缓存键前缀常量
    private static final String CACHE_KEY_VERIFICATION_PREFIX = "springoj:auth:verification:";
    private static final String LOCK_KEY_LOGIN_PREFIX = "springoj:lock:login:";
    private static final String LOCK_KEY_REGISTER_PREFIX = "springoj:lock:register:";

    public final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenMapper tokenMapper;
    private final EmailService emailService;
    private final DistributedLockManager distributedLockManager;
    private final RedisUtils redisUtils;

    @Override
    @Transactional
    public Token login(String username, String password) {
        AuthUser user = userMapper.loadUserByUsername(username);
        if (user == null) {
            log.error("用户不存在");
            throw BizException.of(ResponseCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("密码错误");
            throw BizException.of(ResponseCode.USERNAME_OR_PASSWORD_ERROR);
        }
        String accessToken = jwtService.generateToken(username);
        Token token =
                Token.builder()
                        .userId(user.getUserId())
                        .token(accessToken)
                        .tokenType(TokenType.ACCESS)
                        .build();
        // 使用分布式锁防止并发重复写入
        distributedLockManager.executeWithLock(
                LOCK_KEY_LOGIN_PREFIX + username,
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                () -> {
                    tokenMapper.insert(token);
                });
        return token;
    }

    @Override
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        // 使用 Redis 缓存服务，设置 5 分钟过期
        String codeKey = CACHE_KEY_VERIFICATION_PREFIX + email;
        redisUtils.strings().set(codeKey, code, Duration.ofMinutes(5));
        emailService.sendVerificationCode(email, code);
    }

    @Override
    @Transactional
    public void register(String username, String password, String email, String code) {
        log.debug("register: {} {} {} {}", username, password, email, code);
        String codeKey = CACHE_KEY_VERIFICATION_PREFIX + email;
        String storedCode = redisUtils.strings().getString(codeKey);
        if (storedCode == null || !storedCode.equals(code)) {
            throw BizException.of(ResponseCode.BUSINESS_ERROR.getCode(), "验证码错误或已过期");
        }

        if (userMapper.loadUserByUsername(username) != null) {
            throw BizException.of(ResponseCode.BUSINESS_ERROR.getCode(), "用户名已存在");
        }

        AuthUser user =
                AuthUser.builder()
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .email(email)
                        .status(1)
                        .build();
        // 使用分布式锁防止并发重复注册
        distributedLockManager.executeWithLock(
                LOCK_KEY_REGISTER_PREFIX + username,
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                () -> {
                    userMapper.insert(user);
                });
        // 注册完成后删除验证码
        redisUtils.strings().delete(codeKey);
    }

    @Override
    @Transactional
    @RedisCacheable(
            key = "'user:token:' + #token",
            ttl = 1800, // 30分钟缓存
            type = AuthUser.class,
            keyPrefix = "springoj:cache:")
    public AuthUser validateToken(String token) {
        try {
            final String username = jwtService.extractUsername(token);
            if (username == null || username.isEmpty()) {
                log.error("JWT token无效或缺少用户名");
                throw BizException.of(ResponseCode.INVALID_TOKEN);
            }

            AuthUser userDetails = userMapper.loadUserByUsername(username);
            if (userDetails == null) {
                log.error("用户不存在: {}", username);
                throw BizException.of(ResponseCode.RC401.getCode(), "未找到用户");
            }

            if (!jwtService.isTokenValid(token, userDetails)) {
                log.error("Token验证失败: {}", username);
                throw BizException.of(ResponseCode.INVALID_TOKEN);
            }

            Token res = tokenMapper.findValidToken(userDetails.getUserId(), token);
            if (res == null) {
                log.error("用户token无效或已被撤销: {}", username);
                throw BizException.of(ResponseCode.INVALID_TOKEN);
            }

            return userDetails;
        } catch (ExpiredJwtException e) {
            log.error("Token已过期", e);
            throw BizException.of(ResponseCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token无效", e);
            throw BizException.of(ResponseCode.INVALID_TOKEN);
        }
    }

    @Override
    @RedisEvict(
            keys = {"'user:info:' + #username", "'user:token:' + #token"},
            keyPrefix = "springoj:cache:")
    public void logout(String username, String token) {
        tokenMapper.deleteByToken(token);
    }

    @Override
    @RedisCacheable(
            key = "'user:info:' + #username",
            ttl = 1800, // 30分钟缓存
            type = AuthUser.class,
            keyPrefix = "springoj:cache:")
    public AuthUser getUserInfo(String username) {
        return userMapper.loadUserByUsername(username);
    }
}
