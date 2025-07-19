package com.david.controller;

import com.david.entity.request.LoginRequest;
import com.david.entity.request.RegisterRequest;
import com.david.entity.token.TokenValidateRequest;
import com.david.entity.token.Token;
import com.david.entity.user.AuthUser;
import com.david.service.AuthService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户认证、权限和角色管理相关的请求
 *
 * @author david
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/login")
    public ResponseResult<Token> login(@RequestBody LoginRequest loginRequest) {
        return ResponseResult.success(authService.login(loginRequest.getUsername(), loginRequest.getPassword()));
    }

    @PostMapping("/register")
    public ResponseResult<Void> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail(), registerRequest.getCode());
        return ResponseResult.success();
    }

    @PostMapping("/send-code")
    public ResponseResult<Void> sendCode(@RequestParam String email) {
        authService.sendVerificationCode(email);
        return ResponseResult.success();
    }

    @PostMapping("/validate")
    public ResponseResult<AuthUser> validateToken(@RequestBody TokenValidateRequest request) {
        log.debug("内部token验证请求: {}", request.getToken().substring(0, Math.min(request.getToken().length(), 20)));
        AuthUser authUser = authService.validateToken(request.getToken());
        log.debug("Token验证成功，用户: {}", authUser.getUsername());
        return ResponseResult.success(authUser);
    }
}