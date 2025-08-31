package com.david.redis.commons.aspect.chain.config;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.aspect.chain.AspectChainManager;
import com.david.redis.commons.aspect.chain.AspectHandler;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 切面处理器链配置类
 *
 * <p>负责配置和管理切面处理器的注册和执行顺序。
 *
 * @author David
 */
@Configuration
@RequiredArgsConstructor
public class AspectChainConfiguration {

    /**
     * 创建切面链管理器
     *
     * @param handlers 所有的切面处理器
     * @param logUtils 日志工具
     * @return 切面链管理器
     */
    @Bean
    public AspectChainManager aspectChainManager(List<AspectHandler> handlers, LogUtils logUtils) {
        // Spring会自动收集所有AspectHandler类型的Bean
        // 并按照@Order注解排序
        return new AspectChainManager(logUtils, handlers);
    }
}
