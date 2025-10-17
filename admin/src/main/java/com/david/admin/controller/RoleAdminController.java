package com.david.admin.controller;

import com.david.admin.dto.RoleCreateRequest;
import com.david.admin.dto.RoleDto;
import com.david.admin.dto.RoleUpdateRequest;
import com.david.admin.dto.RoleView;
import com.david.admin.service.RoleManagementService;
import com.david.common.http.ApiResponse;

import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@Validated
@PreAuthorize("hasRole('admin')")
@RequestMapping("/api/admin/roles")
public class RoleAdminController {

    private final RoleManagementService roleManagementService;

    public RoleAdminController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping
    public ApiResponse<List<RoleView>> listRoles(
            @RequestParam(required = false) String keyword) {
        log.info("查询角色列表，关键词: {}", keyword);
        List<RoleView> roles = roleManagementService.listRoles(keyword);
        log.info("查询角色列表成功，共返回 {} 条记录", roles.size());
        return ApiResponse.success(roles);
    }

    @GetMapping("/options")
    public ApiResponse<List<RoleDto>> listRoleOptions() {
        log.info("查询角色选项列表");
        List<RoleDto> roles = roleManagementService.listRoleOptions();
        log.info("查询角色选项列表成功，共返回 {} 条记录", roles.size());
        return ApiResponse.success(roles);
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
    public ApiResponse<RoleView> createRole(@Valid @RequestBody RoleCreateRequest request) {
        log.info("创建角色，请求参数: {}", request);
        RoleView roleView = roleManagementService.createRole(request);
        log.info("创建角色成功，角色ID: {}, 角色编码: {}", roleView.id(), roleView.code());
        return ApiResponse.success(roleView);
    }

    @PutMapping("/{roleId}")
    public ApiResponse<RoleView> updateRole(
            @PathVariable Long roleId, @Valid @RequestBody RoleUpdateRequest request) {
        log.info("更新角色，角色ID: {}, 请求参数: {}", roleId, request);
        RoleView roleView = roleManagementService.updateRole(roleId, request);
        log.info("更新角色成功，角色ID: {}, 角色编码: {}", roleView.id(), roleView.code());
        return ApiResponse.success(roleView);
    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        log.info("删除角色，角色ID: {}", roleId);
        roleManagementService.deleteRole(roleId);
        log.info("删除角色成功，角色ID: {}", roleId);
        return ApiResponse.success(null);
    }
}
