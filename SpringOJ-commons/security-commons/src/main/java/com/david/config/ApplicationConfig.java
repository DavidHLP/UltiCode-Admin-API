package com.david.config;

import com.david.entity.user.AuthUser;
import com.david.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 应用程序配置类，提供下游服务的基础配置。
 * 下游服务不需要从数据库验证用户，直接从网关传递的请求头获取用户信息。
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

  /**
   * 配置 AuditorAware，用于审计实体的创建者和修改者。
   * 从请求头获取当前用户信息用于审计。
   *
   * @return AuditorAware 实例。
   */
  @Bean
  public AuditorAware<Integer> auditorAware() {
    return new ApplicationAuditAware();
  }

  /**
   * 配置 AuthenticationManager，用于管理认证（虽然下游服务主要依赖网关认证）。
   *
   * @param config AuthenticationConfiguration 实例。
   * @return AuthenticationManager 实例。
   * @throws Exception 如果获取失败。
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * 配置 PasswordEncoder，主要用于兼容性（下游服务通常不需要密码验证）。
   *
   * @return PasswordEncoder 实例。
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * 提供当前请求范围内的 AuthUser Bean
   * 便于在其他组件中直接注入使用
   *
   * @return 当前用户的 AuthUser 对象，如果未找到则返回 null
   */
  @Bean
  @RequestScope
  public AuthUser currentAuthUser() {
    // 首先尝试从 Security 上下文获取
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
      return (AuthUser) authentication.getPrincipal();
    }

    // 如果 Security 上下文中没有，尝试从请求头获取
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      Optional<AuthUser> authUserOpt = UserContextUtil.getCurrentAuthUser(request);
      return authUserOpt.orElse(null);
    }

    return null;
  }
}