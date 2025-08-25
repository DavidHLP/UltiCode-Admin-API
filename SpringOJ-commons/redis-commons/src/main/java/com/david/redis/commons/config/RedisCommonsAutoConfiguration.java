package com.david.redis.commons.config;

import com.david.redis.commons.aspect.CacheAspect;
import com.david.redis.commons.aspect.TransactionAspect;
import com.david.redis.commons.core.cache.CacheConditionEvaluator;
import com.david.redis.commons.core.cache.CacheKeyGenerator;
import com.david.redis.commons.core.DistributedLockManager;
import com.david.redis.commons.core.RedisTransactionManager;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.properties.RedisCommonsProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis Commons 自动配置类
 *
 * @author david
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({ RedisTemplate.class, RedissonClient.class })
@ConditionalOnProperty(prefix = "spring.data.redis.commons", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisCommonsProperties.class)
@Import({
        RedisConfig.class,
        RedissonConfig.class
})
public class RedisCommonsAutoConfiguration {

    public RedisCommonsAutoConfiguration() {
        log.info("Redis Commons 自动配置已启用");
    }

    /**
     * 配置Redis工具类
     *
     * @param redisTemplate Redis模板
     * @return RedisUtils实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisUtils.class)
    public RedisUtils redisUtils(RedisTemplate<String, Object> redisTemplate) {
        log.info("配置Redis工具类");
        return new RedisUtils(redisTemplate);
    }

    /**
     * 配置RedisUtils的事务管理器依赖
     * 使用@Autowired来避免循环依赖
     */
    @Autowired(required = false)
    public void configureRedisUtilsTransactionManager(RedisUtils redisUtils,
            RedisTransactionManager transactionManager) {
        if (transactionManager != null) {
            redisUtils.setTransactionManager(transactionManager);
            log.debug("为RedisUtils设置事务管理器");
        }
    }

    /**
     * 配置Redis事务管理器
     *
     * @param redisTemplate Redis模板
     * @return RedisTransactionManager实例
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean(RedisTransactionManager.class)
    @ConditionalOnProperty(prefix = "spring.data.redis.commons.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisTransactionManager redisTransactionManager(RedisTemplate<String, Object> redisTemplate) {
        log.info("配置Redis事务管理器");
        return new RedisTransactionManager(redisTemplate);
    }

    /**
     * 配置事务切面
     *
     * @param transactionManager Redis事务管理器
     * @return TransactionAspect实例
     */
    @Bean
    @ConditionalOnBean(RedisTransactionManager.class)
    @ConditionalOnMissingBean(TransactionAspect.class)
    @ConditionalOnProperty(prefix = "spring.data.redis.commons.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TransactionAspect transactionAspect(RedisTransactionManager transactionManager) {
        log.info("配置Redis事务切面");
        return new TransactionAspect(transactionManager);
    }

    /**
     * 配置分布式锁管理器
     *
     * @param redissonClient         Redisson客户端
     * @param redisCommonsProperties Redis Commons配置属性
     * @return DistributedLockManager实例
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean(DistributedLockManager.class)
    public DistributedLockManager distributedLockManager(
            RedissonClient redissonClient,
            RedisCommonsProperties redisCommonsProperties) {
        log.info("配置分布式锁管理器");
        return new DistributedLockManager(redissonClient, redisCommonsProperties);
    }

    /**
     * 配置缓存键生成器
     *
     * @return CacheKeyGenerator实例
     */
    @Bean
    @ConditionalOnMissingBean(CacheKeyGenerator.class)
    public CacheKeyGenerator cacheKeyGenerator() {
        log.info("配置缓存键生成器");
        return new CacheKeyGenerator();
    }

    /**
     * 配置缓存条件评估器
     *
     * @return CacheConditionEvaluator实例
     */
    @Bean
    @ConditionalOnMissingBean(CacheConditionEvaluator.class)
    public CacheConditionEvaluator cacheConditionEvaluator() {
        log.info("配置缓存条件评估器");
        return new CacheConditionEvaluator();
    }

    /**
     * 配置缓存切面
     *
     * @param redisUtils         Redis工具类
     * @param keyGenerator       缓存键生成器
     * @param conditionEvaluator 缓存条件评估器
     * @param properties         Redis Commons配置属性
     * @return CacheAspect实例
     */
    @Bean
    @ConditionalOnBean({ RedisUtils.class, CacheKeyGenerator.class, CacheConditionEvaluator.class })
    @ConditionalOnMissingBean(CacheAspect.class)
    public CacheAspect cacheAspect(RedisUtils redisUtils,
            CacheKeyGenerator keyGenerator,
            CacheConditionEvaluator conditionEvaluator,
            RedisCommonsProperties properties) {
        log.info("配置Redis缓存切面");
        return new CacheAspect(redisUtils, keyGenerator, conditionEvaluator, properties);
    }
}