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
import java.util.List;
import org.springframework.http.HttpStatus;
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

@RestController
@Validated
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
        PageResult<UserView> result =
                userManagementService.listUsers(page, size, keyword, status, roleId);
        return ApiResponse.success(result);
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserView> getUser(@PathVariable Long userId) {
        return ApiResponse.success(userManagementService.getUser(userId));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserView> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(userManagementService.createUser(request));
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<UserView> updateUser(
            @PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userManagementService.updateUser(userId, request));
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleDto>> listRoles() {
        return ApiResponse.success(userManagementService.listRoles());
    }
}
