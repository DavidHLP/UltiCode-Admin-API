package com.david.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long userId;
    private String Username;
    private String avatar;
    @Default
    private String introduction = "用户未填写";
    private String email;
    private Integer status;
    @Default
    private String address = "用户未填写";
    private String lastLoginIp;
    private LocalDateTime lastLogin;
    private Long roleId;
    private String roleName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;
    private String password;
}