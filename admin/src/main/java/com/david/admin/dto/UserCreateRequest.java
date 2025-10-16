package com.david.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserCreateRequest(
        @NotBlank(message = "用户名不能为空")
                @Size(min = 3, max = 64, message = "用户名长度需在3到64之间")
                String username,
        @NotBlank(message = "邮箱不能为空")
                @Email(message = "邮箱格式不正确")
                @Size(max = 254, message = "邮箱长度不能超过254")
                String email,
        @NotBlank(message = "密码不能为空")
                @Size(min = 6, max = 100, message = "密码长度需在6到100之间")
                String password,
        @Size(max = 255, message = "头像链接长度不能超过255")
                String avatarUrl,
        @Size(max = 255, message = "个人简介长度不能超过255")
                String bio,
        Integer status,
        List<Long> roleIds) {}
