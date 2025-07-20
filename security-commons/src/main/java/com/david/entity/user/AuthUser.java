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

    public AuthUser(Long userId, String username, String password, Integer status, Role role, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
        this.email = email;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (role != null && role.getRoleName() != null && !role.getRoleName().isEmpty()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
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

    public boolean hasRole(String roleName) {
        return role != null && Objects.equals(role.getRoleName(), roleName);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public String getRoleName() {
        return role != null ? role.getRoleName() : null;
    }
}