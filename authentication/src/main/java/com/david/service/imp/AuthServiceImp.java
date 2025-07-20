package com.david.service.imp;

import com.david.entity.user.User;
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
import com.david.utils.RedisCacheUtil;
import com.david.utils.RedisLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    public final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenMapper tokenMapper;
    private final RedisLockUtil redisLockUtil;
    private final RedisCacheUtil redisCacheUtil;
    private final EmailService emailService;

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
        redisLockUtil.executeWithWriteLock(RedisLocks.LOGIN+username, () -> {
            tokenMapper.insert(token);
            return null;
        });
        return token;
    }

    @Override
    public void sendVerificationCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        redisCacheUtil.set(RedisCacheKeys.VERIFICATION_CODE_KEY_PREFIX + email, code, 5 * 60); // 5 minutes expiration
        emailService.sendVerificationCode(email, code);
    }

    @Override
    @Transactional
    public void register(String username, String password, String email, String code) {
        String storedCode = (String) redisCacheUtil.get(RedisCacheKeys.VERIFICATION_CODE_KEY_PREFIX + email);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        if (userMapper.loadUserByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .status(1)
                .build();
        redisLockUtil.executeWithWriteLock(RedisLocks.REGISTER+username, () -> {
            userMapper.insert(user);
            return null;
        });
        redisCacheUtil.del(RedisCacheKeys.VERIFICATION_CODE_KEY_PREFIX + email);
    }

    @Override
    @Transactional
    public AuthUser validateToken(String jwt) {
        final String username = jwtService.extractUsername(jwt);
        if (username == null || username.isEmpty()) {
            log.error("JWT token无效或缺少用户名");
            throw new UsernameNotFoundException("JWT token无效或缺少用户名");
        }

        return redisLockUtil.executeWithLock(RedisLocks.VALIDATETOKEN+username, () -> {
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
        });
    }

    @Override
    public void logout(String username ,String token) {
        redisLockUtil.executeWithWriteLock(RedisLocks.LOGOUT+username, () -> {
            tokenMapper.deleteByToken(token);
            return null;
        });
    }
}