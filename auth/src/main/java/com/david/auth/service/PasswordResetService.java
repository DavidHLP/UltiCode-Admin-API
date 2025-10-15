package com.david.auth.service;

import com.david.auth.entity.User;

public interface PasswordResetService {

    void sendPasswordResetEmail(User user);
}
