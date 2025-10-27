package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.admin.dto.PageResult;
import com.david.admin.dto.RoleDto;
import com.david.admin.dto.UserCreateRequest;
import com.david.admin.dto.UserUpdateRequest;
import com.david.admin.dto.UserView;
import com.david.admin.entity.Role;
import com.david.admin.entity.User;
import com.david.admin.entity.UserRole;
import com.david.admin.mapper.RoleMapper;
import com.david.admin.mapper.UserMapper;
import com.david.admin.mapper.UserRoleMapper;
import com.david.core.exception.BusinessException;
import com.david.core.forward.ForwardedUser;
import com.david.core.security.AuditAction;
import com.david.core.security.SecurityAuditRecord;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditTrailService auditTrailService;

    public UserManagementService(
            UserMapper userMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder,
            AuditTrailService auditTrailService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditTrailService = auditTrailService;
    }

    public PageResult<UserView> listUsers(
            int page, int size, String keyword, Integer status, Long roleId) {
        Page<User> pager = new Page<>(page, size);

        LambdaQueryWrapper<User> query = Wrappers.lambdaQuery(User.class);
        if (keyword != null && !keyword.isBlank()) {
            query.and(
                    wrapper ->
                            wrapper.like(User::getUsername, keyword)
                                    .or()
                                    .like(User::getEmail, keyword));
        }
        if (status != null) {
            query.eq(User::getStatus, status);
        }
        if (roleId != null) {
            List<Long> userIdsByRole = findUserIdsByRole(roleId);
            if (userIdsByRole.isEmpty()) {
                return new PageResult<>(List.of(), 0, page, size);
            }
            query.in(User::getId, userIdsByRole);
        }
        query.orderByDesc(User::getCreatedAt);

        Page<User> result = userMapper.selectPage(pager, query);
        List<User> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), result.getTotal(), page, size);
        }
        Map<Long, List<Role>> roles =
                loadRolesByUserIds(records.stream().map(User::getId).toList());
        List<UserView> items =
                records.stream()
                        .map(user -> toUserView(user, roles.getOrDefault(user.getId(), List.of())))
                        .toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public UserView getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        List<Role> roles = loadRolesByUserIds(List.of(userId)).getOrDefault(userId, List.of());
        return toUserView(user, roles);
    }

    @Transactional
    public UserView createUser(ForwardedUser principal, UserCreateRequest request) {
        ensureUniqueUsername(request.username(), null);
        ensureUniqueEmail(request.email(), null);

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAvatarUrl(request.avatarUrl());
        user.setBio(request.bio());
        user.setStatus(request.status() == null ? 1 : request.status());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        List<Long> roleIds = normalizeRoleIds(request.roleIds());
        if (!roleIds.isEmpty()) {
            replaceUserRoles(user.getId(), roleIds);
        }
        recordUserAudit(principal, user.getId(), "创建用户");
        return getUser(user.getId());
    }

    @Transactional
    public UserView updateUser(ForwardedUser principal, Long userId, UserUpdateRequest request) {
        User existing = userMapper.selectById(userId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        if (request.username() != null
                && !Objects.equals(request.username(), existing.getUsername())) {
            ensureUniqueUsername(request.username(), userId);
        }
        if (request.email() != null && !Objects.equals(request.email(), existing.getEmail())) {
            ensureUniqueEmail(request.email(), userId);
        }

        LambdaUpdateWrapper<User> update =
                Wrappers.lambdaUpdate(User.class).eq(User::getId, userId);

        if (request.username() != null) {
            update.set(User::getUsername, request.username());
        }
        if (request.email() != null) {
            update.set(User::getEmail, request.email());
        }
        if (request.password() != null && !request.password().isBlank()) {
            update.set(User::getPasswordHash, passwordEncoder.encode(request.password()));
        }
        if (request.avatarUrl() != null) {
            update.set(User::getAvatarUrl, request.avatarUrl());
        }
        if (request.bio() != null) {
            update.set(User::getBio, request.bio());
        }
        if (request.status() != null) {
            update.set(User::getStatus, request.status());
        }
        update.set(User::getUpdatedAt, LocalDateTime.now());

        userMapper.update(null, update);

        boolean rolesAdjusted = false;
        if (request.roleIds() != null) {
            List<Long> roleIds = normalizeRoleIds(request.roleIds());
            replaceUserRoles(userId, roleIds);
            rolesAdjusted = true;
        }

        if (principal != null) {
            String description = rolesAdjusted ? "调整用户角色" : "更新用户资料";
            recordUserAudit(principal, userId, description);
        }
        return getUser(userId);
    }

    private void ensureUniqueField(
            String value,
            Long excludeUserId,
            Function<User, Object> fieldGetter,
            String errorMessage) {
        if (value == null || value.isBlank()) {
            return;
        }
        LambdaQueryWrapper<User> query =
                Wrappers.lambdaQuery(User.class).eq((SFunction<User, ?>) fieldGetter, value);
        if (excludeUserId != null) {
            query.ne(User::getId, excludeUserId);
        }
        Long count = userMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, errorMessage);
        }
    }

    private void ensureUniqueUsername(String username, Long excludeUserId) {
        ensureUniqueField(username, excludeUserId, User::getUsername, "用户名已存在");
    }

    private void ensureUniqueEmail(String email, Long excludeUserId) {
        ensureUniqueField(email, excludeUserId, User::getEmail, "邮箱已存在");
    }

    private Map<Long, List<Role>> loadRolesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserRole> relations =
                userRoleMapper.selectList(
                        Wrappers.lambdaQuery(UserRole.class).in(UserRole::getUserId, userIds));
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> roleIds = relations.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Role> roles = roleMapper.selectByIds(roleIds);
        Map<Long, Role> roleMap =
                roles == null
                        ? Collections.emptyMap()
                        : roles.stream().collect(Collectors.toMap(Role::getId, role -> role));

        Map<Long, List<Role>> result = new HashMap<>();
        for (UserRole relation : relations) {
            Role role = roleMap.get(relation.getRoleId());
            if (role == null) {
                continue;
            }
            result.computeIfAbsent(relation.getUserId(), key -> new ArrayList<>()).add(role);
        }
        return result;
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Role> roles = roleMapper.selectByIds(roleIds);
        if (roles == null || roles.size() != new HashSet<>(roleIds).size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效的角色ID");
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long roleId : roleIds) {
            UserRole relation = new UserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setCreatedAt(now);
            userRoleMapper.insert(relation);
        }
    }

    private List<Long> normalizeRoleIds(List<Long> roleIds) {
        if (roleIds == null) {
            return List.of();
        }
        return roleIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private List<Long> findUserIdsByRole(Long roleId) {
        List<UserRole> relations =
                userRoleMapper.selectList(
                        Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getRoleId, roleId));
        if (relations == null || relations.isEmpty()) {
            return List.of();
        }
        return relations.stream().map(UserRole::getUserId).distinct().toList();
    }

    private void recordUserAudit(ForwardedUser principal, Long targetUserId, String description) {
        if (principal == null) {
            return;
        }
        auditTrailService.record(
                SecurityAuditRecord.builder()
                        .actorId(principal.id())
                        .actorUsername(principal.username())
                        .action(AuditAction.USER_ROLE_CHANGED)
                        .objectType("user")
                        .objectId(String.valueOf(targetUserId))
                        .description(description)
                        .build());
    }

    private UserView toUserView(User user, List<Role> roles) {
        List<RoleDto> roleDtos =
                roles == null ? List.of() : roles.stream().map(this::toRoleDto).toList();
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                roleDtos);
    }

    private RoleDto toRoleDto(Role role) {
        return new RoleDto(role.getId(), role.getCode(), role.getName());
    }
}
