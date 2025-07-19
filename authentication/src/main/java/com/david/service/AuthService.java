package com.david.service;

import com.david.entity.token.Token;
import com.david.entity.user.AuthUser;

public interface AuthService {
    Token login(String username, String password);
    void register(String username, String password , String email , String code);
    void sendVerificationCode(String email);
    AuthUser validateToken(String token);
    void logout(String username ,String token);
}
