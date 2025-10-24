package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.admin.dto.PermissionCreateRequest;
import com.david.admin.dto.PermissionDto;
import com.david.admin.entity.Permission;
import com.david.admin.exception.BusinessException;
import com.david.admin.mapper.PermissionMapper;
import com.david.common.forward.ForwardedUser;
import com.david.common.security.AuditAction;
import com.david.common.security.SecurityAuditRecord;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionManagementService {

    private final PermissionMapper permissionMapper;
    private final AuditTrailService auditTrailService;

    public PermissionManagementService(
            PermissionMapper permissionMapper, AuditTrailService auditTrailService) {
        this.permissionMapper = permissionMapper;
        this.auditTrailService = auditTrailService;
    }

    public List<PermissionDto> listPermissions(String keyword) {
        LambdaQueryWrapper<Permission> query = Wrappers.lambdaQuery(Permission.class);
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(wrapper -> wrapper.like(Permission::getCode, trimmed).or().like(Permission::getName, trimmed));
        }
        query.orderByAsc(Permission::getCode);
        List<Permission> permissions = permissionMapper.selectList(query);
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        return permissions.stream().map(this::toDto).toList();
    }

    @Transactional
    public PermissionDto createPermission(ForwardedUser principal, PermissionCreateRequest request) {
        ensureCodeUnique(request.code(), null);
        Permission permission = new Permission();
        permission.setCode(request.code().trim());
        permission.setName(request.name().trim());
        permission.setCreatedAt(LocalDateTime.now());
        permissionMapper.insert(permission);
        recordAudit(principal, permission.getId(), "创建权限");
        return toDto(permission);
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
