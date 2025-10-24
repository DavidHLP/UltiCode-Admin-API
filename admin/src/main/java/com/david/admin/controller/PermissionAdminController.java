package com.david.admin.controller;

import com.david.admin.dto.PermissionCreateRequest;
import com.david.admin.dto.PermissionDto;
import com.david.admin.service.PermissionManagementService;
import com.david.admin.service.SensitiveOperationGuard;
import com.david.common.forward.ForwardedUser;
import com.david.common.http.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ApiResponse<List<PermissionDto>> listPermissions(@RequestParam(required = false) String keyword) {
        List<PermissionDto> permissions = permissionManagementService.listPermissions(keyword);
        return ApiResponse.success(permissions);
    }

    @PostMapping
    public ApiResponse<PermissionDto> createPermission(
            @AuthenticationPrincipal ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @Valid @RequestBody PermissionCreateRequest request) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        PermissionDto created = permissionManagementService.createPermission(principal, request);
        log.info("创建权限成功，code={} by user {}", created.code(), principal.username());
        return ApiResponse.success(created);
    }

    @DeleteMapping("/{permissionId}")
    public ApiResponse<Void> deletePermission(
            @AuthenticationPrincipal ForwardedUser principal,
            @RequestHeader("X-Sensitive-Action-Token") String sensitiveToken,
            @PathVariable Long permissionId) {
        sensitiveOperationGuard.ensureValid(principal.id(), sensitiveToken);
        permissionManagementService.deletePermission(principal, permissionId);
        log.info("删除权限成功，permissionId={} by user {}", permissionId, principal.username());
        return ApiResponse.success(null);
    }
}
