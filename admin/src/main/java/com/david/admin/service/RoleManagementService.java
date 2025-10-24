package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.admin.dto.PermissionDto;
import com.david.admin.dto.RoleCreateRequest;
import com.david.admin.dto.RoleDto;
import com.david.admin.dto.RoleUpdateRequest;
import com.david.admin.dto.RoleView;
import com.david.admin.entity.Permission;
import com.david.admin.entity.Role;
import com.david.admin.entity.RolePermission;
import com.david.admin.entity.UserRole;
import com.david.admin.exception.BusinessException;
import com.david.admin.mapper.PermissionMapper;
import com.david.admin.mapper.RoleMapper;
import com.david.admin.mapper.RolePermissionMapper;
import com.david.admin.mapper.UserRoleMapper;
import com.david.common.forward.ForwardedUser;
import com.david.common.security.AuditAction;
import com.david.common.security.SecurityAuditRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final AuditTrailService auditTrailService;
    private final PermissionChangeNotifier permissionChangeNotifier;

    public RoleManagementService(
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            PermissionMapper permissionMapper,
            RolePermissionMapper rolePermissionMapper,
            AuditTrailService auditTrailService,
            PermissionChangeNotifier permissionChangeNotifier) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.auditTrailService = auditTrailService;
        this.permissionChangeNotifier = permissionChangeNotifier;
    }

    public List<RoleView> listRoles(String keyword) {
        LambdaQueryWrapper<Role> query = Wrappers.lambdaQuery(Role.class);
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(
                    wrapper ->
                            wrapper.like(Role::getCode, trimmed)
                                    .or()
                                    .like(Role::getName, trimmed));
        }
        query.orderByDesc(Role::getCreatedAt);
        List<Role> roles = roleMapper.selectList(query);
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Permission>> permissionsMap =
                loadPermissionsByRoleIds(roles.stream().map(Role::getId).toList());
        return roles.stream()
                .map(role -> toRoleView(role, permissionsMap.getOrDefault(role.getId(), List.of())))
                .toList();
    }

    public List<RoleDto> listRoleOptions() {
        LambdaQueryWrapper<Role> query =
                Wrappers.lambdaQuery(Role.class).orderByAsc(Role::getId);
        List<Role> roles = roleMapper.selectList(query);
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream().map(this::toRoleDto).toList();
    }

    public RoleView getRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "角色不存在");
        }
        Map<Long, List<Permission>> permissionsMap = loadPermissionsByRoleIds(List.of(roleId));
        return toRoleView(role, permissionsMap.getOrDefault(roleId, List.of()));
    }

    @Transactional
    public RoleView createRole(ForwardedUser principal, RoleCreateRequest request) {
        String code = normalizeCode(request.code());
        String name = normalizeName(request.name());
        String remark = normalizeRemark(request.remark());

        ensureCodeUnique(code, null);

        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setRemark(remark);
        LocalDateTime now = LocalDateTime.now();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);

        roleMapper.insert(role);
        replaceRolePermissions(role.getId(), normalizePermissionIds(request.permissionIds()));
        recordRoleAudit(principal, role.getId(), "创建角色");
        permissionChangeNotifier.notifyRoleChange(role.getId(), role.getCode());
        return getRole(role.getId());
    }

    @Transactional
    public RoleView updateRole(ForwardedUser principal, Long roleId, RoleUpdateRequest request) {
        Role existing = roleMapper.selectById(roleId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "角色不存在");
        }

        boolean changed = false;

        if (request.code() != null) {
            String code = normalizeCode(request.code());
            if (!Objects.equals(code, existing.getCode())) {
                ensureCodeUnique(code, roleId);
                existing.setCode(code);
                changed = true;
            }
        }

        if (request.name() != null) {
            String name = normalizeName(request.name());
            if (!Objects.equals(name, existing.getName())) {
                existing.setName(name);
                changed = true;
            }
        }

        if (request.remark() != null) {
            String remark = normalizeRemark(request.remark());
            if (!Objects.equals(remark, existing.getRemark())) {
                existing.setRemark(remark);
                changed = true;
            }
        }

        if (!changed) {
            if (request.permissionIds() != null) {
                replaceRolePermissions(roleId, normalizePermissionIds(request.permissionIds()));
                recordRoleAudit(principal, roleId, "更新角色权限集合");
                permissionChangeNotifier.notifyRoleChange(roleId, existing.getCode());
            }
            Map<Long, List<Permission>> permMap = loadPermissionsByRoleIds(List.of(roleId));
            return toRoleView(existing, permMap.getOrDefault(roleId, List.of()));
        }

        existing.setUpdatedAt(LocalDateTime.now());
        LambdaUpdateWrapper<Role> update =
                Wrappers.lambdaUpdate(Role.class).eq(Role::getId, roleId);
        update.set(Role::getCode, existing.getCode());
        update.set(Role::getName, existing.getName());
        update.set(Role::getRemark, existing.getRemark());
        update.set(Role::getUpdatedAt, existing.getUpdatedAt());

        roleMapper.update(null, update);
        if (request.permissionIds() != null) {
            replaceRolePermissions(roleId, normalizePermissionIds(request.permissionIds()));
        }
        recordRoleAudit(principal, roleId, "更新角色基本信息");
        permissionChangeNotifier.notifyRoleChange(roleId, existing.getCode());
        return getRole(roleId);
    }

    @Transactional
    public void deleteRole(ForwardedUser principal, Long roleId) {
        Role existing = roleMapper.selectById(roleId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "角色不存在");
        }
        Long relationCount =
                userRoleMapper.selectCount(
                        Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getRoleId, roleId));
        if (relationCount != null && relationCount > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "仍有用户关联该角色，无法删除");
        }
        rolePermissionMapper.delete(
                Wrappers.lambdaQuery(RolePermission.class).eq(RolePermission::getRoleId, roleId));
        roleMapper.deleteById(roleId);
        recordRoleAudit(principal, roleId, "删除角色");
        permissionChangeNotifier.notifyRoleChange(roleId, existing.getCode());
    }

    private void ensureCodeUnique(String code, Long excludeRoleId) {
        LambdaQueryWrapper<Role> query =
                Wrappers.lambdaQuery(Role.class).eq(Role::getCode, code);
        if (excludeRoleId != null) {
            query.ne(Role::getId, excludeRoleId);
        }
        Long count = roleMapper.selectCount(query);
        if (count != null && count > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "角色编码已存在");
        }
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "角色编码不能为空");
        }
        return trimmed;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "角色名称不能为空");
        }
        return trimmed;
    }

    private String normalizeRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RoleView toRoleView(Role role, List<Permission> permissions) {
        List<PermissionDto> permissionDtos =
                permissions == null
                        ? List.of()
                        : permissions.stream()
                                .map(p -> new PermissionDto(p.getId(), p.getCode(), p.getName()))
                                .toList();
        return new RoleView(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getRemark(),
                role.getCreatedAt(),
                role.getUpdatedAt(),
                permissionDtos);
    }

    private RoleDto toRoleDto(Role role) {
        return new RoleDto(role.getId(), role.getCode(), role.getName());
    }

    private List<Long> normalizePermissionIds(List<Long> permissionIds) {
        if (permissionIds == null) {
            return List.of();
        }
        return permissionIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(
                Wrappers.lambdaQuery(RolePermission.class).eq(RolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
        if (permissions == null || permissions.size() != permissionIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在无效的权限ID");
        }
        for (Long permissionId : permissionIds) {
            RolePermission relation = new RolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        }
    }

    private Map<Long, List<Permission>> loadPermissionsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        List<RolePermission> relations =
                rolePermissionMapper.selectList(
                        Wrappers.lambdaQuery(RolePermission.class).in(RolePermission::getRoleId, roleIds));
        if (relations == null || relations.isEmpty()) {
            return Map.of();
        }
        Set<Long> permissionIds =
                relations.stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
        if (permissionIds.isEmpty()) {
            return Map.of();
        }
        List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
        Map<Long, Permission> permissionMap =
                permissions == null
                        ? Map.of()
                        : permissions.stream()
                                .collect(Collectors.toMap(Permission::getId, permission -> permission));
        Map<Long, List<Permission>> result = new HashMap<>();
        for (RolePermission relation : relations) {
            Permission permission = permissionMap.get(relation.getPermissionId());
            if (permission == null) {
                continue;
            }
            result.computeIfAbsent(relation.getRoleId(), key -> new ArrayList<>()).add(permission);
        }
        return result;
    }

    private void recordRoleAudit(ForwardedUser principal, Long roleId, String description) {
        if (principal == null) {
            return;
        }
        auditTrailService.record(
                SecurityAuditRecord.builder()
                        .actorId(principal.id())
                        .actorUsername(principal.username())
                        .action(AuditAction.ROLE_PERMISSION_UPDATED)
                        .objectType("role")
                        .objectId(String.valueOf(roleId))
                        .description(description)
                        .build());
    }
}
