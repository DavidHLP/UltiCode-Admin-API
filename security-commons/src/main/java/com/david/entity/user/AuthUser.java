package com.david.entity.user;

import com.david.entity.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUser implements UserDetails {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private Integer status;
    private Role role;
    private List<String> authorities;

    public AuthUser(Long userId, String username, String password, Integer status, Role role, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
        this.email = email;
    }

    /**
     * 获取用户权限列表
     * 注意：这个方法返回的是Spring Security的GrantedAuthority对象
     * 而authorities字段存储的是权限字符串列表，用于JSON序列化
     */
    @Override
    @JsonIgnore // 在JSON序列化时忽略此方法，避免与authorities字段冲突
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // 添加角色权限
        if (role != null && role.getRoleName() != null && !role.getRoleName().isEmpty()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        }

        // 添加角色的权限列表
        if (role != null && role.getPermissions() != null) {
            role.getPermissions().stream()
                    .filter(p -> Objects.nonNull(p) && Objects.nonNull(p.getPermission()))
                    .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                    .forEach(grantedAuthorities::add);
        }

        // 添加额外的权限（authorities字段应该只包含权限字符串，不是GrantedAuthority对象）
        if (authorities != null) {
            authorities.stream()
                    .filter(Objects::nonNull)
                    .filter(auth -> !auth.trim().isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(grantedAuthorities::add);
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
        if (!(o instanceof AuthUser)) {
            return false;
        }
        AuthUser authUser = (AuthUser) o;
        return Objects.equals(userId, authUser.userId) &&
                Objects.equals(email, authUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    /**
     * 检查是否具有指定角色
     */
    public boolean hasRole(String roleName) {
        return role != null && Objects.equals(role.getRoleName(), roleName);
    }

    /**
     * 检查是否具有管理员权限
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查是否具有指定权限
     */
    public boolean hasAuthority(String authority) {
        return getAuthorities().stream()
                .anyMatch(auth -> Objects.equals(auth.getAuthority(), authority));
    }

    /**
     * 获取角色名称
     */
    public String getRoleName() {
        return role != null ? role.getRoleName() : null;
    }

    /**
     * 为了确保序列化正确，这里明确说明 authorities 字段应该是字符串列表
     * 而不是 GrantedAuthority 对象列表
     */
    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    /**
     * 清理权限列表，移除空值和空字符串
     */
    public void cleanAuthorities() {
        if (authorities != null) {
            authorities = authorities.stream()
                    .filter(Objects::nonNull)
                    .filter(auth -> !auth.trim().isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}
