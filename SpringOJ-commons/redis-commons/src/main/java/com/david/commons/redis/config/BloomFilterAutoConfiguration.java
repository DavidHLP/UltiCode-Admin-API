package com.david.commons.redis.config;

import com.david.commons.redis.cache.protection.interfaces.BloomFilterManager;
import com.david.commons.redis.cache.protection.nexts.RedissonBloomFilterManager;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器自动配置
 *
 * @author David
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
        prefix = "spring.redis.commons.protection",
        name = "enable-bloom-filter",
        havingValue = "true",
        matchIfMissing = true)
public class BloomFilterAutoConfiguration {

    /** 布隆过滤器管理器 */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public BloomFilterManager bloomFilterManager(
            RedissonClient redissonClient, RedisCommonsProperties properties) {
        log.info("正在配置基于 Redisson 的布隆过滤器管理器");
        return new RedissonBloomFilterManager(redissonClient, properties);
    }
}
