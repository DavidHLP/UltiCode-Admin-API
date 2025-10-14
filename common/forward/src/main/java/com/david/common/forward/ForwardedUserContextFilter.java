package com.david.common.forward;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Populates the {@link org.springframework.security.core.context.SecurityContext} with the user
 * identity that has already been authenticated by the gateway.
 */
@Order(ForwardedUserContextFilter.ORDER)
public class ForwardedUserContextFilter extends OncePerRequestFilter {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication currentAuthentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (currentAuthentication == null || !currentAuthentication.isAuthenticated()) {
            Optional<ForwardedUser> forwardedUser = ForwardedUserParser.from(request);
            forwardedUser.ifPresent(
                    user -> {
                        List<SimpleGrantedAuthority> authorities =
                                user.roles().stream()
                                        .filter(StringUtils::hasText)
                                        .map(
                                                role ->
                                                        role.startsWith("ROLE_")
                                                                ? role
                                                                : "ROLE_" + role)
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());
                        ForwardedAuthenticationToken authentication =
                                new ForwardedAuthenticationToken(user, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
        filterChain.doFilter(request, response);
    }
}
