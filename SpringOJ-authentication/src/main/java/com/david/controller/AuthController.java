package com.david.controller;

import com.david.entity.request.LoginRequest;
import com.david.entity.request.LogoutRequest;
import com.david.entity.request.RegisterRequest;
import com.david.entity.token.Token;
import com.david.entity.user.AuthUser;
import com.david.service.AuthService;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 处理用户认证、权限和角色管理相关的请求
 *
 * @author david
 */
@Slf4j
@RestController
@RequestMapping("/auth/api")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseResult<Token> login(@RequestBody LoginRequest loginRequest) {
        return ResponseResult.success(
                "登录成功", authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseResult<Void> logout(@RequestBody LogoutRequest logoutRequest) {
        authService.logout(getCurrentUsername(), logoutRequest.getToken());
        return ResponseResult.success("登出成功");
    }

    @PostMapping("/register")
    public ResponseResult<Void> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                registerRequest.getCode());
        return ResponseResult.success("注册成功");
    }

    @PostMapping("/send-code")
    public ResponseResult<Void> sendCode(@RequestParam String email) {
        authService.sendVerificationCode(email);
        return ResponseResult.success();
    }

    @GetMapping("/validate/{token}")
    public ResponseResult<AuthUser> validateToken(@PathVariable("token") String token) {
        AuthUser authUser = authService.validateToken(token);
        return ResponseResult.success("验证Token成功", authUser);
    }

    @GetMapping("/me")
    public ResponseResult<AuthUser> getUserInfo() {
        String email = getCurrentUserEmail();
        AuthUser authUser = authService.getUserInfo(email);
        return ResponseResult.success("获取用户信息成功", authUser);
    }
}