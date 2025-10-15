package com.david.auth.service;

public interface RegistrationVerificationService {

    void sendRegistrationCode(String email);

    void verifyRegistrationCode(String email, String verificationCode);
}
