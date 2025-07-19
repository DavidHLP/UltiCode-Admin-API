package com.david.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Spring Security WebFlux 配置类
 * 简化的 Gateway 安全配置，主要权限验证由 AuthGlobalFilter 处理
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    /**
     * 公开路径，无需认证
     */
    private static final String[] PUBLIC_PATHS = {
            "/actuator/**",
            "/favicon.ico",
            "/error",
            "/api/auth/login/**",
            "/api/auth/register/**",
            "/api/auth/refresh/**",
            "/api/auth/validate/**",
            "/api/auth/send-code/**"
    };

    /**
     * 配置 WebFlux 安全过滤器链
     * 简化配置，主要权限验证交给 AuthGlobalFilter 处理
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用 CSRF（API 网关不需要）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // 禁用表单登录
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // 禁用 HTTP Basic 认证
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // 配置无状态会话管理
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                // 简化权限配置，允许所有请求通过，具体权限由 AuthGlobalFilter 控制
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .anyExchange().permitAll() // 改为 permitAll，由 AuthGlobalFilter 控制
                )
                .build();
    }
}