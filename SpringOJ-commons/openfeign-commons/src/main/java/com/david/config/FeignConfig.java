package com.david.config;

import feign.RequestInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 * 注册权限信息传递拦截器
 * 不依赖security-commons，保持模块独立性
 */
@Configuration
public class FeignConfig {

    /**
     * 注册Feign权限拦截器
     * 确保所有Feign调用都自动传递权限信息
     */
    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}