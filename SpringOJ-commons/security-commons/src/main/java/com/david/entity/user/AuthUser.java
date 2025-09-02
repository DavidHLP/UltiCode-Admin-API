package com.david.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.entity.role.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class AuthUser implements UserDetails {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;
    private String avatar;
    @Default private String introduction = "用户未填写";
    private String email;
    private Integer status;
    @Default private String address = "用户未填写";
    private String lastLoginIp;
    private LocalDateTime lastLogin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @TableField(exist = false)
    private List<Role> roles;

    @Deprecated
    @TableField(exist = false)
    private Role role;

    public AuthUser(
            Long userId,
            String username,
            String password,
            Integer status,
            Role role,
            String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.email = email;
        if (role != null) {
            this.roles = new ArrayList<>(Collections.singletonList(role));
        } else {
            this.roles = new ArrayList<>();
        }
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (roles != null) {
            for (Role r : roles) {
                if (r != null && r.getRoleName() != null && !r.getRoleName().isEmpty()) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + r.getRoleName()));
                }
            }
        }
        if (role != null && role.getRoleName() != null && !role.getRoleName().isEmpty()) {
            SimpleGrantedAuthority single =
                    new SimpleGrantedAuthority("ROLE_" + role.getRoleName());
            if (!grantedAuthorities.contains(single)) {
                grantedAuthorities.add(single);
            }
        }
        return grantedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Objects.nonNull(status) && status == 1;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthUser authUser)) {
            return false;
        }
        return Objects.equals(userId, authUser.userId) && Objects.equals(email, authUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    public boolean hasRole(String roleName) {
        if (roleName == null) return false;
        if (roles != null) {
            for (Role r : roles) {
                if (r != null && roleName.equals(r.getRoleName())) {
                    return true;
                }
            }
        }
        return role != null && roleName.equals(role.getRoleName());
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public String getRoleName() {
        if (roles != null && !roles.isEmpty() && roles.get(0) != null) {
            return roles.get(0).getRoleName();
        }
        return role != null ? role.getRoleName() : null;
    }
}
