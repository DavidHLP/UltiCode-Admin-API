package com.david.service;

public interface EmailService {
    void sendVerificationCode(String email, String code);
}
