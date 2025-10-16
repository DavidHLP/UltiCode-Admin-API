package com.david.common.forward;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** 简单的值对象，用于描述已经通过上游网关认证的用户身份。 */
public record ForwardedUser(Long id, String username, List<String> roles) implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    public ForwardedUser {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    /** 便利工厂方法，避免空集合。 */
    public static ForwardedUser of(Long id, String username, List<String> roles) {
        return new ForwardedUser(id, username, roles == null ? Collections.emptyList() : roles);
    }

    public boolean hasRole(String role) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream().filter(Objects::nonNull).anyMatch(role::equals);
    }
}
