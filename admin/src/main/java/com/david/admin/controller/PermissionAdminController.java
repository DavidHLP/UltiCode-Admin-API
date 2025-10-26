package com.david.admin.controller;

import com.david.admin.dto.PermissionCreateRequest;
import com.david.admin.dto.PermissionUpdateRequest;
import com.david.admin.dto.PermissionView;
import com.david.admin.service.PermissionManagementService;
import com.david.admin.service.SensitiveOperationGuard;
import com.david.core.security.CurrentForwardedUser;
import com.david.core.forward.ForwardedUser;
import com.david.core.http.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('platform_admin')")
@RequestMapping("/api/admin/permissions")
public class PermissionAdminController {

    private final PermissionManagementService permissionManagementService;
    private final SensitiveOperationGuard sensitiveOperationGuard;

    @GetMapping
    public ApiResponse<List<PermissionView>> listPermissions(@RequestParam(required = false) String keyword) {
        List<PermissionView> permissions = permissionManagementService.listPermissionViews(keyword);
        return ApiResponse.success(permissions);
    }

    @PostMapping
    public ApiResponse<PermissionView> createPermission(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @Valid @RequestBody PermissionCreateRequest request) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        PermissionView created = permissionManagementService.createPermission(principal, request);
        log.info("创建权限成功，code={} by user {}", created.code(), principal.username());
        return ApiResponse.success(created);
    }

    @PutMapping("/{permissionId}")
    public ApiResponse<PermissionView> updatePermission(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long permissionId,
            @Valid @RequestBody PermissionUpdateRequest request) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        PermissionView updated = permissionManagementService.updatePermission(principal, permissionId, request);
        log.info("更新权限成功，permissionId={} by user {}", permissionId, principal.username());
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{permissionId}")
    public ApiResponse<Void> deletePermission(
            @CurrentForwardedUser ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long permissionId) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        permissionManagementService.deletePermission(principal, permissionId);
        log.info("删除权限成功，permissionId={} by user {}", permissionId, principal.username());
        return ApiResponse.success(null);
    }
}
