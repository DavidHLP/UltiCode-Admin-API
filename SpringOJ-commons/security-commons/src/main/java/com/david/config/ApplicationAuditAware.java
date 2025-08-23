package com.david.config;

import com.david.utils.UserContextUtil;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 审计感知器实现类
 * 用于 JPA 审计功能，从请求头获取当前用户信息
 * 下游服务通过网关传递的用户信息进行审计
 */
@Slf4j
public class ApplicationAuditAware implements AuditorAware<Integer> {

    @Override
    public Optional<Integer> getCurrentAuditor() {
        try {
            // 首先尝试从 Spring Security 上下文获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                log.debug("从 Security 上下文获取审计用户: {}", authentication.getName());
                return Optional.of(1); // 这里可以根据实际需求返回用户ID
            }

            // 如果 Security 上下文中没有，尝试从请求头获取
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Optional<Long> userId = UserContextUtil.getCurrentUserId(request);
                Optional<String> username = UserContextUtil.getCurrentUsername(request);

                if (userId.isPresent() && username.isPresent()) {
                    log.debug("从请求头获取审计用户: {} (ID: {})", username.get(), userId.get());
                    return Optional.of(userId.get().intValue());
                }
            }

            log.debug("未找到当前用户信息，使用默认审计用户");
            return Optional.of(0); // 默认系统用户ID

        } catch (Exception e) {
            log.warn("获取当前审计用户失败: {}", e.getMessage());
            return Optional.of(0); // 异常情况下使用默认用户ID
        }
    }
}
