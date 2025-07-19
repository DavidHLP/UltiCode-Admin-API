package com.david.config;

import com.david.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * 下游服务安全配置
 * 依赖网关传递的用户信息，不进行独立的用户认证
 * 启用方法级安全，支持 @PreAuthorize、@PostAuthorize 等注解
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnMissingClass("org.springframework.web.reactive.config.WebFluxConfigurer")
public class SecurityConfiguration {
    @Value("${spring.security.auth.whitelist}")
    private String[] AUTH_WHITELIST;

    private final AuthenticationFilter gatewayAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加网关认证过滤器，从请求头获取用户信息
                .addFilterBefore(gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用 CSRF，因为是无状态服务
                .csrf(csrf -> csrf.disable())
                // 禁用默认的登录页面和HTTP基本认证
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 配置安全头
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(contentTypeOptions -> {})
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)))
                // 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"请求头中缺少有效的用户信息\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"权限不足\"}");
                        }));

        return http.build();
    }
}