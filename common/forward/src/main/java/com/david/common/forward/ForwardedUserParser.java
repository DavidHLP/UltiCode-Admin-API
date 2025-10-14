package com.david.common.forward;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Utilities that read the forwarded user information from headers. */
public final class ForwardedUserParser {

    private ForwardedUserParser() {}

    public static Optional<ForwardedUser> from(HttpServletRequest request) {
        return from(request::getHeader);
    }

    public static Optional<ForwardedUser> from(HttpHeaders headers) {
        return from(headers::getFirst);
    }

    private static Optional<ForwardedUser> from(Function<String, String> headerProvider) {
        String idHeader = headerProvider.apply(ForwardedUserHeaders.USER_ID);
        String username = headerProvider.apply(ForwardedUserHeaders.USER_NAME);
        String rolesHeader = headerProvider.apply(ForwardedUserHeaders.USER_ROLES);

        if (!StringUtils.hasText(idHeader) || !StringUtils.hasText(username)) {
            return Optional.empty();
        }
        Long userId = parseUserId(idHeader);
        List<String> roles = parseRoles(rolesHeader);
        return Optional.of(ForwardedUser.of(userId, username, roles));
    }

    private static Long parseUserId(String idHeader) {
        try {
            return Long.valueOf(idHeader);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<String> parseRoles(@Nullable String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(ForwardedUserHeaders.ROLE_DELIMITER))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
