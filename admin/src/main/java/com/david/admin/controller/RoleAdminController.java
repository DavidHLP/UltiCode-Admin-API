package com.david.admin.controller;

import com.david.admin.dto.PageResult;
import com.david.admin.dto.PermissionDto;
import com.david.admin.dto.RoleCreateRequest;
import com.david.admin.dto.RoleDto;
import com.david.admin.dto.RoleUpdateRequest;
import com.david.admin.dto.RoleView;
import com.david.admin.service.PermissionManagementService;
import com.david.admin.service.RoleManagementService;
import com.david.admin.service.SensitiveOperationGuard;
import com.david.core.security.CurrentForwardedUser;
import com.david.core.forward.ForwardedUser;
import com.david.core.http.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@Validated
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/roles")
public class RoleAdminController {

    private final RoleManagementService roleManagementService;
    private final SensitiveOperationGuard sensitiveOperationGuard;
    private final PermissionManagementService permissionManagementService;

    public RoleAdminController(
            RoleManagementService roleManagementService,
            SensitiveOperationGuard sensitiveOperationGuard,
            PermissionManagementService permissionManagementService) {
        this.roleManagementService = roleManagementService;
        this.sensitiveOperationGuard = sensitiveOperationGuard;
        this.permissionManagementService = permissionManagementService;
    }

    @GetMapping
    public ApiResponse<PageResult<RoleView>> listRoles(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String remark,
            @RequestParam(name = "permissionIds", required = false) List<Long> permissionIds,
            @RequestParam(required = false) Long permissionId) {
        Set<Long> permissionFilter = new LinkedHashSet<>();
        if (permissionIds != null) {
            permissionFilter.addAll(permissionIds);
        }
        if (permissionId != null) {
            permissionFilter.add(permissionId);
        }
        log.info(
                "查询角色列表，页码: {}, 大小: {}, 关键词: {}, 编码: {}, 名称: {}, 备注: {}, 权限集合: {}",
                page,
                size,
                keyword,
                code,
                name,
                remark,
                permissionFilter.isEmpty() ? null : permissionFilter);
        PageResult<RoleView> roles =
                roleManagementService.listRoles(
                        page, size, keyword, code, name, remark, permissionFilter);
        log.info("查询角色列表成功，共返回 {} 条记录", roles.total());
        return ApiResponse.success(roles);
    }

    @GetMapping("/options")
    public ApiResponse<List<RoleDto>> listRoleOptions() {
        log.info("查询角色选项列表");
        List<RoleDto> roles = roleManagementService.listRoleOptions();
        log.info("查询角色选项列表成功，共返回 {} 条记录", roles.size());
        return ApiResponse.success(roles);
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionDto>> listRolePermissions(
            @RequestParam(required = false) String keyword) {
        log.info("查询角色可用权限列表，关键词: {}", keyword);
        List<PermissionDto> permissions = permissionManagementService.listPermissionsDto(keyword);
        log.info("查询权限列表成功，共返回 {} 条记录", permissions.size());
        return ApiResponse.success(permissions);
    }

    @GetMapping("/{roleId}")
    public ApiResponse<RoleView> getRole(@PathVariable Long roleId) {
        log.info("获取角色详情，角色ID: {}", roleId);
        RoleView roleView = roleManagementService.getRole(roleId);
        log.info("获取角色详情成功，角色编码: {}", roleView.code());
        return ApiResponse.success(roleView);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoleView> createRole(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @Valid @RequestBody RoleCreateRequest request) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        log.info("创建角色，请求参数: {}", request);
        RoleView roleView = roleManagementService.createRole(principal, request);
        log.info("创建角色成功，角色ID: {}, 角色编码: {}", roleView.id(), roleView.code());
        return ApiResponse.success(roleView);
    }

    @PutMapping("/{roleId}")
    public ApiResponse<RoleView> updateRole(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long roleId,
            @Valid @RequestBody RoleUpdateRequest request) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        log.info("更新角色，角色ID: {}, 请求参数: {}", roleId, request);
        RoleView roleView = roleManagementService.updateRole(principal, roleId, request);
        log.info("更新角色成功，角色ID: {}, 角色编码: {}", roleView.id(), roleView.code());
        return ApiResponse.success(roleView);
    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> deleteRole(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long roleId) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        log.info("删除角色，角色ID: {}", roleId);
        roleManagementService.deleteRole(principal, roleId);
        log.info("删除角色成功，角色ID: {}", roleId);
        return ApiResponse.success(null);
    }
}
