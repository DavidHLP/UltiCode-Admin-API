package com.david.auth.service;

import com.david.auth.dto.RegisterRequest;
import com.david.auth.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User register(RegisterRequest request);

    Optional<User> findByUsernameOrEmail(String identifier);

    User getActiveUser(Long userId);

    List<String> findRoleCodes(Long userId);

    void updateLoginMetadata(Long userId, String ipAddress);

    String hashPassword(String rawPassword);
}
