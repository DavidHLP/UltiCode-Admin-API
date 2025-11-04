package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.admin.dto.PageResult;
import com.david.admin.dto.PermissionCreateRequest;
import com.david.admin.dto.PermissionDto;
import com.david.admin.dto.PermissionUpdateRequest;
import com.david.admin.dto.PermissionView;
import com.david.admin.entity.Permission;
import com.david.admin.mapper.PermissionMapper;
import com.david.core.exception.BusinessException;
import com.david.core.forward.ForwardedUser;
import com.david.core.security.AuditAction;
import com.david.core.security.SecurityAuditRecord;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PermissionManagementService {

    private final PermissionMapper permissionMapper;
    private final AuditTrailService auditTrailService;

    public PermissionManagementService(
            PermissionMapper permissionMapper, AuditTrailService auditTrailService) {
        this.permissionMapper = permissionMapper;
        this.auditTrailService = auditTrailService;
    }

    public List<PermissionDto> listPermissionsDto(String keyword) {
        return listPermissions(keyword).stream().map(this::toDto).toList();
    }

    public PageResult<PermissionView> listPermissionViews(
            int page,
            int size,
            String keyword,
            String code,
            String name,
            LocalDate createdAtStart,
            LocalDate createdAtEnd) {
        Page<Permission> pager = new Page<>(page, size);
        LambdaQueryWrapper<Permission> query = Wrappers.lambdaQuery(Permission.class);

        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            if (!trimmed.isEmpty()) {
                query.and(
                        wrapper ->
                                wrapper.like(Permission::getCode, trimmed)
                                        .or()
                                        .like(Permission::getName, trimmed));
            }
        }
        if (code != null && !code.isBlank()) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) {
                query.like(Permission::getCode, trimmed);
            }
        }
        if (name != null && !name.isBlank()) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                query.like(Permission::getName, trimmed);
            }
        }
        if (createdAtStart != null) {
            query.ge(Permission::getCreatedAt, createdAtStart.atStartOfDay());
        }
        if (createdAtEnd != null) {
            query.lt(Permission::getCreatedAt, createdAtEnd.plusDays(1).atStartOfDay());
        }
        query.orderByDesc(Permission::getCreatedAt);

        Page<Permission> result = permissionMapper.selectPage(pager, query);
        List<Permission> records = result.getRecords();
        List<PermissionView> views =
                (records == null || records.isEmpty())
                        ? List.of()
                        : records.stream().map(this::toView).toList();
        return new PageResult<>(views, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private List<Permission> listPermissions(String keyword) {
        LambdaQueryWrapper<Permission> query = Wrappers.lambdaQuery(Permission.class);
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper ->
                            wrapper.like(Permission::getCode, trimmed)
                                    .or()
                                    .like(Permission::getName, trimmed));
        }
        query.orderByAsc(Permission::getCode);
        List<Permission> permissions = permissionMapper.selectList(query);
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        return permissions;
    }

    @Transactional
    public PermissionView createPermission(
            ForwardedUser principal, PermissionCreateRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();
        ensureCodeUnique(code, null);
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission.setCreatedAt(LocalDateTime.now());
        permissionMapper.insert(permission);
        recordAudit(principal, permission.getId(), "创建权限");
        return toView(permission);
    }

    @Transactional
    public PermissionView updatePermission(
            ForwardedUser principal, Long permissionId, PermissionUpdateRequest request) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "权限不存在");
        }
        String newCode = request.code().trim();
        String newName = request.name().trim();
        if (!permission.getCode().equals(newCode)) {
            ensureCodeUnique(newCode, permissionId);
            permission.setCode(newCode);
        } else {
            permission.setCode(newCode);
        }
        permission.setName(newName);
        permissionMapper.updateById(permission);
        recordAudit(principal, permissionId, "更新权限");
        Permission refreshed = permissionMapper.selectById(permissionId);
        return toView(refreshed != null ? refreshed : permission);
    }

    @Transactional
    public void deletePermission(ForwardedUser principal, Long permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "权限不存在");
        }
        permissionMapper.deleteById(permissionId);
        recordAudit(principal, permissionId, "删除权限");
    }

    private PermissionDto toDto(Permission permission) {
        return new PermissionDto(permission.getId(), permission.getCode(), permission.getName());
    }

    private PermissionView toView(Permission permission) {
        return new PermissionView(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getCreatedAt());
    }

    private void ensureCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<Permission> query =
                Wrappers.lambdaQuery(Permission.class).eq(Permission::getCode, code);
        if (excludeId != null) {
            query.ne(Permission::getId, excludeId);
        }
        Long count = permissionMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "权限编码已存在");
        }
    }

    private void recordAudit(ForwardedUser principal, Long permissionId, String description) {
        if (principal == null) {
            return;
        }
        auditTrailService.record(
                SecurityAuditRecord.builder()
                        .actorId(principal.id())
                        .actorUsername(principal.username())
                        .action(AuditAction.SECURITY_POLICY_CHANGED)
                        .objectType("permission")
                        .objectId(String.valueOf(permissionId))
                        .description(description)
                        .build());
    }
}
