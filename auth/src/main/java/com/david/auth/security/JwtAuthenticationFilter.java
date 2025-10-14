package com.david.auth.security;

import com.david.auth.entity.TokenKind;
import com.david.auth.entity.User;
import com.david.auth.service.TokenService;
import com.david.auth.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   TokenService tokenService,
                                   UserService userService) {
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull  HttpServletRequest request,
                                    @NonNull  HttpServletResponse response,
                                    @NonNull  FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Jws<Claims> jws = jwtService.parseAndValidate(token);
                Claims claims = jws.getBody();
                if (jwtService.isExpired(claims)) {
                    throw new BadCredentialsException("Token expired");
                }
                TokenKind kind = jwtService.extractTokenKind(claims);
                if (!kind.isAccess()) {
                    throw new BadCredentialsException("Access token required");
                }
                tokenService.findActiveToken(token, TokenKind.ACCESS)
                        .orElseThrow(() -> new BadCredentialsException("Token revoked or invalid"));
                Long userId = jwtService.extractUserId(claims);
                User user = userService.getActiveUser(userId);
                List<String> roles = userService.findRoleCodes(userId);

                UserPrincipal principal = new UserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        user.getPasswordHash(),
                        roles
                );

                var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException | BadCredentialsException ex) {
                request.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", ex);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
