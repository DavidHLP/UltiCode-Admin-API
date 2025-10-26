package com.david.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.auth.dto.RegisterRequest;
import com.david.auth.entity.Role;
import com.david.auth.entity.User;
import com.david.auth.entity.UserRole;
import com.david.common.http.exception.BusinessException;
import com.david.auth.mapper.RoleMapper;
import com.david.auth.mapper.UserMapper;
import com.david.auth.mapper.UserRoleMapper;
import com.david.auth.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE_CODE = "user";
    private static final String DEFAULT_ROLE_NAME = "Regular User";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserMapper userMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        validateUniqueness(request.username(), request.email());

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        Role role = ensureDefaultRole();
        bindUserRole(user.getId(), role.getId());
        return userMapper.selectById(user.getId());
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        LambdaQueryWrapper<User> query =
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getUsername, identifier)
                        .or()
                        .eq(User::getEmail, identifier);
        return Optional.ofNullable(userMapper.selectOne(query));
    }

    @Override
    public User getActiveUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED, "User is disabled or does not exist");
        }
        return user;
    }

    @Override
    public List<String> findRoleCodes(Long userId) {
        LambdaQueryWrapper<UserRole> userRoleQuery =
                Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleQuery);
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Role> roles = roleMapper.selectByIds(roleIds);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream().map(Role::getCode).toList();
    }

    @Override
    @Transactional
    public void updateLoginMetadata(Long userId, String ipAddress) {
        LambdaUpdateWrapper<User> update =
                Wrappers.lambdaUpdate(User.class)
                        .eq(User::getId, userId)
                        .set(User::getLastLoginAt, LocalDateTime.now())
                        .set(User::getLastLoginIp, ipAddress);
        userMapper.update(null, update);
    }

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private void validateUniqueness(String username, String email) {
        Long usernameCount =
                userMapper.selectCount(
                        Wrappers.lambdaQuery(User.class).eq(User::getUsername, username));
        if (usernameCount != null && usernameCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Username already exists");
        }
        Long emailCount =
                userMapper.selectCount(Wrappers.lambdaQuery(User.class).eq(User::getEmail, email));
        if (emailCount != null && emailCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already exists");
        }
    }

    private Role ensureDefaultRole() {
        Role role =
                roleMapper.selectOne(
                        Wrappers.lambdaQuery(Role.class).eq(Role::getCode, DEFAULT_ROLE_CODE));
        if (role != null) {
            return role;
        }
        Role newRole = new Role();
        newRole.setCode(DEFAULT_ROLE_CODE);
        newRole.setName(DEFAULT_ROLE_NAME);
        newRole.setRemark("Auto-created default role");
        newRole.setCreatedAt(LocalDateTime.now());
        newRole.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(newRole);
        return newRole;
    }

    private void bindUserRole(Long userId, Long roleId) {
        UserRole relation = new UserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        relation.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(relation);
    }
}
