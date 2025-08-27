package com.david.redis.commons.config;

import com.david.redis.commons.aspect.CacheAspect;
import com.david.redis.commons.aspect.TransactionAspect;
import com.david.redis.commons.core.cache.CacheConditionEvaluator;
import com.david.redis.commons.core.cache.CacheKeyGenerator;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.core.lock.DistributedLockManager;
import com.david.redis.commons.core.operations.RedisLockOperationsImpl;
import com.david.redis.commons.core.operations.interfaces.RedisLockOperations;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.manager.BatchOperationManager;
import com.david.redis.commons.manager.CacheWarmUpManager;
import com.david.redis.commons.monitor.CacheMetricsCollector;
import com.david.redis.commons.properties.RedisCommonsProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
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
     * 配置Redis操作执行器
     */
    @Bean
    @ConditionalOnMissingBean(RedisOperationExecutor.class)
    public RedisOperationExecutor redisOperationExecutor(RedisLoggerHelper loggerHelper) {
        log.info("配置Redis操作执行器");
        return new RedisOperationExecutor(loggerHelper);
    }

    /**
     * 配置Redis结果处理器
     */
    @Bean
    @ConditionalOnMissingBean(RedisResultProcessor.class)
    public RedisResultProcessor redisResultProcessor() {
        log.info("配置Redis结果处理器");
        return new RedisResultProcessor();
    }

    /**
     * 配置Redis日志助手
     */
    @Bean
    @ConditionalOnMissingBean(RedisLoggerHelper.class)
    public RedisLoggerHelper redisLoggerHelper() {
        log.info("配置Redis日志助手");
        return new RedisLoggerHelper();
    }

    /**
     * 配置Redis工具类
     *
     * @param redisTemplate Redis模板
     * @return RedisUtils实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisUtils.class)
    public RedisUtils redisUtils(RedisTemplate<String, Object> redisTemplate,
            RedisLockOperations lockOperations,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper,
            RedisTransactionManager transactionManager) {
        log.info("配置Redis工具类");
        return new RedisUtils(redisTemplate, lockOperations, executor, resultProcessor, loggerHelper,
                transactionManager);
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
    @ConditionalOnBean({ RedisTransactionManager.class, RedisUtils.class, CacheKeyGenerator.class, CacheMetricsCollector.class })
    @ConditionalOnMissingBean(TransactionAspect.class)
    @ConditionalOnProperty(prefix = "spring.data.redis.commons.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TransactionAspect transactionAspect(RedisTransactionManager transactionManager,
            RedisUtils redisUtils,
            CacheKeyGenerator cacheKeyGenerator,
            CacheMetricsCollector metricsCollector) {
        log.info("配置Redis事务切面");
        return new TransactionAspect(transactionManager, redisUtils, cacheKeyGenerator, metricsCollector);
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
     * 配置Redis锁操作
     *
     * @param redisTemplate          Redis模板
     * @param transactionManager     事务管理器
     * @param executor               操作执行器
     * @param resultProcessor        结果处理器
     * @param loggerHelper           日志助手
     * @param distributedLockManager 分布式锁管理器
     * @param redissonClient         Redisson客户端
     * @param redisCommonsProperties Redis Commons配置属性
     * @return RedisLockOperations实例
     */
    @Bean
    @ConditionalOnBean({ RedisTemplate.class, DistributedLockManager.class, RedissonClient.class })
    @ConditionalOnMissingBean(RedisLockOperations.class)
    public RedisLockOperations redisLockOperations(
            RedisTemplate<String, Object> redisTemplate,
            RedisTransactionManager transactionManager,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper,
            DistributedLockManager distributedLockManager,
            RedissonClient redissonClient,
            RedisCommonsProperties redisCommonsProperties) {
        log.info("配置Redis锁操作");
        return new RedisLockOperationsImpl(redisTemplate, transactionManager, executor,
                resultProcessor, loggerHelper, distributedLockManager, redissonClient, redisCommonsProperties);
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
     * 配置批量操作管理器
     */
    @Bean
    @ConditionalOnBean(RedisUtils.class)
    @ConditionalOnMissingBean(BatchOperationManager.class)
    public BatchOperationManager batchOperationManager(RedisUtils redisUtils) {
        log.info("配置Redis批量操作管理器");
        return new BatchOperationManager(redisUtils);
    }

    /**
     * 配置缓存预热管理器
     */
    @Bean
    @ConditionalOnBean({ RedisUtils.class, BatchOperationManager.class })
    @ConditionalOnMissingBean(CacheWarmUpManager.class)
    public CacheWarmUpManager cacheWarmUpManager(RedisUtils redisUtils, BatchOperationManager batchManager) {
        log.info("配置Redis缓存预热管理器");
        return new CacheWarmUpManager(redisUtils, batchManager);
    }

    /**
     * 配置缓存性能监控收集器
     */
    @Bean
    @ConditionalOnMissingBean(CacheMetricsCollector.class)
    public CacheMetricsCollector cacheMetricsCollector() {
        log.info("配置Redis缓存性能监控收集器");
        return new CacheMetricsCollector();
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
    @ConditionalOnBean({ RedisUtils.class, CacheKeyGenerator.class, CacheConditionEvaluator.class, 
                        BatchOperationManager.class, CacheWarmUpManager.class, CacheMetricsCollector.class })
    @ConditionalOnMissingBean(CacheAspect.class)
    public CacheAspect cacheAspect(RedisUtils redisUtils,
            CacheKeyGenerator keyGenerator,
            CacheConditionEvaluator conditionEvaluator,
            RedisCommonsProperties properties,
            BatchOperationManager batchManager,
            CacheWarmUpManager warmUpManager,
            CacheMetricsCollector metricsCollector) {
        log.info("配置Redis缓存切面");
        return new CacheAspect(redisUtils, keyGenerator, conditionEvaluator, properties, 
                              batchManager, warmUpManager, metricsCollector);
    }
}