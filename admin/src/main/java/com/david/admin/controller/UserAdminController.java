package com.david.admin.controller;

import com.david.admin.dto.PageResult;
import com.david.admin.dto.RoleDto;
import com.david.admin.dto.UserCreateRequest;
import com.david.admin.dto.UserUpdateRequest;
import com.david.admin.dto.UserView;
import com.david.admin.service.UserManagementService;
import com.david.common.http.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/admin")
public class UserAdminController {

    private final UserManagementService userManagementService;

    public UserAdminController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<UserView>> listUsers(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "分页大小不能小于1")
                    @Max(value = 100, message = "分页大小不能超过100")
                    int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long roleId) {
        log.info(
                "查询用户列表，页码: {}, 大小: {}, 关键词: {}, 状态: {}, 角色ID: {}",
                page,
                size,
                keyword,
                status,
                roleId);
        PageResult<UserView> result =
                userManagementService.listUsers(page, size, keyword, status, roleId);
        log.info("查询用户列表成功，共返回 {} 条记录", result.total());
        return ApiResponse.success(result);
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserView> getUser(@PathVariable Long userId) {
        log.info("获取用户详情，用户ID: {}", userId);
        UserView userView = userManagementService.getUser(userId);
        log.info("获取用户详情成功，用户名: {}", userView.username());
        return ApiResponse.success(userView);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserView> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("创建用户，请求参数: {}", request);
        UserView userView = userManagementService.createUser(request);
        log.info("创建用户成功，用户ID: {}, 用户名: {}", userView.id(), userView.username());
        return ApiResponse.success(userView);
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<UserView> updateUser(
            @PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        log.info("更新用户，用户ID: {}, 请求参数: {}", userId, request);
        UserView userView = userManagementService.updateUser(userId, request);
        log.info("更新用户成功，用户ID: {}, 用户名: {}", userView.id(), userView.username());
        return ApiResponse.success(userView);
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleDto>> listRoles() {
        log.info("查询角色列表");
        List<RoleDto> roles = userManagementService.listRoles();
        log.info("查询角色列表成功，共返回 {} 条记录", roles.size());
        return ApiResponse.success(roles);
    }
}
