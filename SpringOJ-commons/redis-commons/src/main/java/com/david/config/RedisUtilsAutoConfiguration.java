package com.david.config;

import com.david.cache.aspect.RedisCacheAspect;
import com.david.service.RedisCacheStringService;
import com.david.service.RedisCacheHashService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis工具类自动装配
 */
@Slf4j
@Configuration
@Import({RedisConfig.class, RedissonConfig.class, RedisCacheAspect.class})
@ConditionalOnClass({RedisTemplate.class, RedissonClient.class})
public class RedisUtilsAutoConfiguration {

    /**
     * Redis String
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({StringRedisTemplate.class, RedissonClient.class})
    public RedisCacheStringService redisCacheStringService(StringRedisTemplate stringRedisTemplate,
                                                          RedissonClient redissonClient) {
        log.info("初始化Redis String缓存服务");
        return new RedisCacheStringService(stringRedisTemplate, redissonClient);
    }

    /**
     * Redis Hash
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({RedisTemplate.class, RedissonClient.class})
    public RedisCacheHashService redisCacheHashService(RedisTemplate<String, Object> redisTemplate,
                                                       RedissonClient redissonClient) {
        log.info("初始化Redis Hash缓存服务");
        return new RedisCacheHashService(redisTemplate, redissonClient);
    }
}