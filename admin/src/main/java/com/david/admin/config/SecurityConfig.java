package com.david.admin.config;

import com.david.common.security.ForwardedAccessDeniedHandler;
import com.david.common.security.ForwardedAuthenticationEntryPoint;
import com.david.common.security.ForwardedSecurityProperties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ForwardedSecurityProperties properties,
            ForwardedAccessDeniedHandler accessDeniedHandler,
            ForwardedAuthenticationEntryPoint authenticationEntryPoint,
            HeaderFilter headerFilter)
            throws Exception {
        log.info("开始配置安全过滤器链");

        if (!properties.isCsrfEnabled()) {
            log.info("CSRF功能已禁用");
            http.csrf(AbstractHttpConfigurer::disable);
        } else {
            log.info("CSRF功能已启用");
        }

        http.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        log.info("设置会话管理策略为无状态");

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        log.info("禁用表单登录、HTTP基本认证和注销功能");

        if (!properties.isAnonymousEnabled()) {
            log.info("匿名访问已禁用");
            http.anonymous(AbstractHttpConfigurer::disable);
        } else {
            log.info("匿名访问已启用");
        }

        http.addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class);
        log.info("注册HeaderFilter用于从请求头恢复认证信息");

        http.exceptionHandling(
                exceptions ->
                        exceptions
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));
        log.info("配置认证入口点和访问拒绝处理器");

        http.authorizeHttpRequests(
                authorize -> {
                    if (properties.isAllowPreflight()) {
                        log.info("允许所有OPTIONS预检请求");
                        authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    }

                    List<String> permitAll = properties.getPermitAll();
                    if (!CollectionUtils.isEmpty(permitAll)) {
                        log.info("配置免认证路径列表，共{}个路径", permitAll.size());
                        permitAll.stream()
                                .map(AntPathRequestMatcher::new)
                                .forEach(
                                        matcher -> {
                                            log.debug("允许访问路径: {}", matcher);
                                            authorize.requestMatchers(matcher).permitAll();
                                        });
                    }

                    log.info("其余所有请求都需要认证");
                    authorize.anyRequest().authenticated();
                });

        log.info("安全过滤器链配置完成");
        return http.build();
    }

    @Bean
    public HeaderFilter headerFilter() {
        return new HeaderFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("创建BCrypt密码编码器，强度为12");
        return new BCryptPasswordEncoder(12);
    }
}
