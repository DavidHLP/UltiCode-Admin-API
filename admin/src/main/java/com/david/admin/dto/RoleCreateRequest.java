package com.david.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoleCreateRequest(
        @NotBlank(message = "角色编码不能为空")
                @Size(max = 64, message = "角色编码长度不能超过64个字符")
                @Pattern(
                        regexp = "^[a-zA-Z0-9:_-]+$",
                        message = "角色编码仅能包含字母、数字、冒号、下划线或破折号")
                String code,
        @NotBlank(message = "角色名称不能为空")
                @Size(max = 128, message = "角色名称长度不能超过128个字符")
                String name,
        @Size(max = 255, message = "备注长度不能超过255个字符") String remark,
        List<Long> permissionIds) {}
