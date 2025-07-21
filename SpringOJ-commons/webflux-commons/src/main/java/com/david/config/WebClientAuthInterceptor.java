package com.david.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * WebClient权限信息传递拦截器
 * 适用于Servlet环境下的WebClient调用
 * 不依赖security-commons，保持模块独立性
 */
@Slf4j
public class WebClientAuthInterceptor implements ExchangeFilterFunction {

    // 用户信息请求头常量（独立定义，不依赖security-commons）
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return Mono.fromCallable(() -> {
            // 获取当前Servlet请求上下文
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpRequest = attributes.getRequest();

                // 构建新的请求，添加权限信息头
                ClientRequest.Builder builder = ClientRequest.from(request);

                String userId = httpRequest.getHeader(USER_ID_HEADER);
                String userName = httpRequest.getHeader(USER_NAME_HEADER);
                String userEmail = httpRequest.getHeader(USER_EMAIL_HEADER);
                String userRoles = httpRequest.getHeader(USER_ROLES_HEADER);

                if (userId != null) {
                    builder.header(USER_ID_HEADER, userId);
                }
                if (userName != null) {
                    builder.header(USER_NAME_HEADER, userName);
                }
                if (userEmail != null) {
                    builder.header(USER_EMAIL_HEADER, userEmail);
                }
                if (userRoles != null) {
                    builder.header(USER_ROLES_HEADER, userRoles);
                }

                if (userId != null || userName != null) {
                    log.debug("WebClient请求成功传递权限信息到: {}", request.url());
                }

                return builder.build();
            }

            log.warn("无法获取当前请求上下文，WebClient调用可能缺少权限信息");
            return request;
        })
                .flatMap(next::exchange);
    }
}
