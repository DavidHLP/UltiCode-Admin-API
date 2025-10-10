package com.david.config;

import com.david.filter.AuthenticationFilter;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** 下游服务安全配置 依赖网关传递的用户信息，不进行独立的用户认证 启用方法级安全，支持 @PreAuthorize、@PostAuthorize 等注解 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity()
public class SecurityConfiguration {
    private final String[] AUTH_WHITELIST = {
        "/actuator/**",
        "/favicon.ico",
        "/error",
        "/auth/api/login/**",
        "/auth/api/register**",
        "/auth/api/refresh/**",
        "/auth/api/validate/**",
        "/auth/api/send-code/**"
    };

    private final AuthenticationFilter gatewayAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(AUTH_WHITELIST)
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加网关认证过滤器，从请求头获取用户信息
                .addFilterBefore(
                        gatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用 CSRF，因为是无状态服务
                .csrf(csrf -> csrf.disable())
                // 禁用默认的登录页面和HTTP基本认证
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 配置安全头
                .headers(
                        headers ->
                                headers.frameOptions(frameOptions -> frameOptions.deny())
                                        .contentTypeOptions(contentTypeOptions -> {})
                                        .httpStrictTransportSecurity(
                                                hstsConfig ->
                                                        hstsConfig
                                                                .maxAgeInSeconds(31536000)
                                                                .includeSubDomains(true)))
                // 配置异常处理
                .exceptionHandling(
                        exceptions ->
                                exceptions
                                        .authenticationEntryPoint(
                                                (request, response, authException) -> {
                                                    response.setStatus(401);
                                                    response.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    ResponseResult<Void> body =
                                                            ResponseResult.fail(
                                                                    ResponseCode.RC401.getCode(),
                                                                    "请求头中缺少有效的用户信息");
                                                    response.getWriter().write(
                                                            toJson(body));
                                                })
                                        .accessDeniedHandler(
                                                (request, response, accessDeniedException) -> {
                                                    response.setStatus(403);
                                                    response.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    ResponseResult<Void> body =
                                                            ResponseResult.fail(
                                                                    ResponseCode.ACCESS_DENIED);
                                                    response.getWriter().write(
                                                            toJson(body));
                                                }));

        return http.build();
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"code\":500,\"message\":\"系统异常\"}";
        }
    }
}
