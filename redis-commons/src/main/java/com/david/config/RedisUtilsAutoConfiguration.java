package com.david.config;

import com.david.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis工具类自动装配
 */
@Slf4j
@Configuration
@Import({RedisConfig.class, RedissonConfig.class})
@ConditionalOnClass({RedisTemplate.class, RedissonClient.class})
public class RedisUtilsAutoConfiguration {

    /**
     * Redis缓存工具类
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisTemplate.class)
    public RedisCacheUtil redisCacheUtil(RedisTemplate<String, Object> redisTemplate) {
        log.info("初始化Redis缓存工具类");
        return new RedisCacheUtil(redisTemplate);
    }

    /**
     * Redis分布式锁工具类
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedissonClient.class)
    public RedisLockUtil redisLockUtil(RedissonClient redissonClient, RedisCacheUtil redisCacheUtil) {
        log.info("初始化Redis分布式锁工具类");
        return new RedisLockUtil(redissonClient, redisCacheUtil);
    }
}