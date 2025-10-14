package com.david.common.forward;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Simple value object describing the user identity that has already been
 * authenticated by the upstream gateway.
 */
public record ForwardedUser(Long id, String username, List<String> roles) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public ForwardedUser {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    /**
     * Convenience factory that avoids null collections.
     */
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
