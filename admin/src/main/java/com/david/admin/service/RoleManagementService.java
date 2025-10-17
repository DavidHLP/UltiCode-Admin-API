package com.david.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.david.admin.dto.RoleCreateRequest;
import com.david.admin.dto.RoleDto;
import com.david.admin.dto.RoleUpdateRequest;
import com.david.admin.dto.RoleView;
import com.david.admin.entity.Role;
import com.david.admin.entity.UserRole;
import com.david.admin.exception.BusinessException;
import com.david.admin.mapper.RoleMapper;
import com.david.admin.mapper.UserRoleMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public RoleManagementService(RoleMapper roleMapper, UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
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
        return roles.stream().map(this::toRoleView).toList();
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
        return toRoleView(role);
    }

    @Transactional
    public RoleView createRole(RoleCreateRequest request) {
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
        return getRole(role.getId());
    }

    @Transactional
    public RoleView updateRole(Long roleId, RoleUpdateRequest request) {
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
            return toRoleView(existing);
        }

        existing.setUpdatedAt(LocalDateTime.now());
        LambdaUpdateWrapper<Role> update =
                Wrappers.lambdaUpdate(Role.class).eq(Role::getId, roleId);
        update.set(Role::getCode, existing.getCode());
        update.set(Role::getName, existing.getName());
        update.set(Role::getRemark, existing.getRemark());
        update.set(Role::getUpdatedAt, existing.getUpdatedAt());

        roleMapper.update(null, update);
        return getRole(roleId);
    }

    @Transactional
    public void deleteRole(Long roleId) {
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
        roleMapper.deleteById(roleId);
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

    private RoleView toRoleView(Role role) {
        return new RoleView(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getRemark(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }

    private RoleDto toRoleDto(Role role) {
        return new RoleDto(role.getId(), role.getCode(), role.getName());
    }
}
