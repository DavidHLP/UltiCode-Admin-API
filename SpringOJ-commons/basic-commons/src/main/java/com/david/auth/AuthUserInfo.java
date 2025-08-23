package com.david.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserInfo {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private Integer status;
    private Role role;
}