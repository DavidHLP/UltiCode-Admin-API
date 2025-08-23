package com.david.service.imp;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.david.service.RedisCacheStringService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.david.entity.token.Token;
import com.david.entity.token.TokenType;
import com.david.entity.user.AuthUser;
import com.david.locks.RedisCacheKeys;
import com.david.locks.RedisLocks;
import com.david.mapper.TokenMapper;
import com.david.mapper.UserMapper;
import com.david.service.AuthService;
import com.david.service.EmailService;
import com.david.utils.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    public final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenMapper tokenMapper;
    private final EmailService emailService;
    private final RedisCacheStringService redisCacheStringService;

    @Override
    @Transactional
    public Token login(String username, String password) {
        AuthUser user = userMapper.loadUserByUsername(username);
        if (user == null) {
            log.error("用户不存在");
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("密码错误");
            throw new RuntimeException("密码错误");
        }
        String accessToken = jwtService.generateToken(username);
        Token token = Token.builder()
                .userId(user.getUserId())
                .token(accessToken)
                .tokenType(TokenType.ACCESS)
                .build();
        // 使用缓存服务的分布式锁能力防止并发重复写入
        redisCacheStringService.getWithLock(RedisLocks.LOGIN + username, 5, 30, TimeUnit.SECONDS, () -> {
            tokenMapper.insert(token);
            return null; // 仅执行受锁逻辑，不回填缓存
        });
        return token;
    }

    @Override
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        // 使用 String 缓存服务，设置 5 分钟过期
        redisCacheStringService.set(RedisCacheKeys.VERIFICATION_CODE_KEY_PREFIX + email, code, 5, TimeUnit.MINUTES);
        emailService.sendVerificationCode(email, code);
    }

    @Override
    @Transactional
    public void register(String username, String password, String email, String code) {
        log.debug("register: {} {} {} {}", username, password, email, code);
        String storedCode = redisCacheStringService.get(RedisCacheKeys.VERIFICATION_CODE_KEY_PREFIX + email);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        if (userMapper.loadUserByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        AuthUser user = AuthUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .status(1)
                .build();
        // 使用分布式锁防止并发重复注册
        redisCacheStringService.getWithLock(RedisLocks.REGISTER + username, 5, 30, TimeUnit.SECONDS, () -> {
            userMapper.insert(user);
            return null; // 仅执行受锁逻辑，不回填缓存
        });
        // 注册完成后删除验证码
        redisCacheStringService.delete(RedisCacheKeys.VERIFICATION_CODE_KEY_PREFIX + email);
    }

    @Override
    @Transactional
    public AuthUser validateToken(String jwt) {
        final String username = jwtService.extractUsername(jwt);
        if (username == null || username.isEmpty()) {
            log.error("JWT token无效或缺少用户名");
            throw new UsernameNotFoundException("JWT token无效或缺少用户名");
        }

        AuthUser userDetails = userMapper.loadUserByUsername(username);
        if (userDetails == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("未找到用户: " + username);
        }

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            log.error("Token验证失败: {}", username);
            throw new UsernameNotFoundException("Token无效或已过期: " + username);
        }

        Token token = tokenMapper.findValidToken(userDetails.getUserId(), jwt);
        if (token == null) {
            log.error("用户token无效或已被撤销: {}", username);
            throw new UsernameNotFoundException("Token无效或已撤销: " + username);
        }

        return userDetails;
    }

    @Override
    public void logout(String username ,String token) {
        tokenMapper.deleteByToken(token);
    }

    @Override
    public AuthUser getUserInfo(String username) {
        return userMapper.loadUserByUsername(username);
    }
}