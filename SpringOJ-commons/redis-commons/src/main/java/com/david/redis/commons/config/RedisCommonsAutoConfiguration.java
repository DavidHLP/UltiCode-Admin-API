package com.david.redis.commons.config;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.aspect.CacheAspect;
import com.david.redis.commons.aspect.chain.AspectChainManager;
import com.david.redis.commons.aspect.chain.AspectHandler;
import com.david.redis.commons.aspect.chain.utils.CacheConditionEvaluator;
import com.david.redis.commons.aspect.chain.utils.CacheKeyGenerator;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.core.operations.lock.DistributedLockManager;
import com.david.redis.commons.core.operations.RedisLockOperationsImpl;
import com.david.redis.commons.core.operations.interfaces.RedisLockOperations;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.manager.BatchOperationManager;
import com.david.redis.commons.manager.CacheWarmUpManager;
import com.david.redis.commons.monitor.CacheMetricsCollector;
import com.david.redis.commons.properties.RedisCommonsProperties;

import lombok.RequiredArgsConstructor;

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

import java.util.List;

/**
 * Redis Commons 自动配置类
 *
 * @author david
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({ RedisTemplate.class, RedissonClient.class })
@ConditionalOnProperty(prefix = "spring.data.redis.commons", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisCommonsProperties.class)
@Import({ RedisConfig.class, RedissonConfig.class })
@RequiredArgsConstructor
public class RedisCommonsAutoConfiguration {
        /**
         * 配置切面处理器链管理器
         *
         * @param allHandlers 所有切面处理器
         * @return AspectChainManager实例
         */
        @Bean
        @ConditionalOnMissingBean(AspectChainManager.class)
        public AspectChainManager aspectChainManager(List<AspectHandler> allHandlers) {
                this.logUtils
                                .business()
                                .event(
                                                "redis_commons",
                                                "bean_creation",
                                                "aspect_chain_manager",
                                                "配置切面处理器链管理器");
                return new AspectChainManager(logUtils, allHandlers);
        }

        /** 配置Redis操作执行器 */
        @Bean
        @ConditionalOnMissingBean(RedisOperationExecutor.class)
        public RedisOperationExecutor redisOperationExecutor(RedisResultProcessor resultProcessor, LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event(
                                                "redis_commons",
                                                "bean_creation",
                                                "redis_operation_executor",
                                                "配置Redis操作执行器");
                return new RedisOperationExecutor(resultProcessor, logUtils);
        }

        /** 配置Redis结果处理器 */
        @Bean
        @ConditionalOnMissingBean(RedisResultProcessor.class)
        public RedisResultProcessor redisResultProcessor() {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "redis_result_processor", "配置Redis结果处理器");
                return new RedisResultProcessor();
        }

        /**
         * 配置Redis工具类
         *
         * @param redisTemplate Redis模板
         * @return RedisUtils实例
         */
        @Bean
        @ConditionalOnMissingBean(RedisUtils.class)
        public RedisUtils redisUtils(
                        RedisTemplate<String, Object> redisTemplate,
                        RedisLockOperations lockOperations,
                        RedisOperationExecutor executor,
                        RedisResultProcessor resultProcessor,
                        RedisTransactionManager transactionManager,
                        LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "redis_utils", "配置Redis工具类");
                return new RedisUtils(
                                redisTemplate,
                                logUtils,
                                lockOperations,
                                executor,
                                resultProcessor,

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
        public RedisTransactionManager redisTransactionManager(
                        RedisTemplate<String, Object> redisTemplate, LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event(
                                                "redis_commons",
                                                "bean_creation",
                                                "redis_transaction_manager",
                                                "配置Redis事务管理器");
                return new RedisTransactionManager(redisTemplate, logUtils);
        }

        /**
         * 配置事务切面
         *
         * @param chainManager 切面处理器链管理器
         * @return TransactionAspect实例
         */
        @Bean
        @ConditionalOnBean(AspectChainManager.class)
        @ConditionalOnMissingBean(TransactionAspect.class)
        @ConditionalOnProperty(prefix = "spring.data.redis.commons.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
        public TransactionAspect transactionAspect(AspectChainManager chainManager) {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "transaction_aspect", "配置Redis事务切面");
                return new TransactionAspect(chainManager, this.logUtils);
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
                        RedisCommonsProperties redisCommonsProperties,
                        LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "distributed_lock_manager", "配置分布式锁管理器");
                return new DistributedLockManager(redissonClient, redisCommonsProperties, logUtils);
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
                        DistributedLockManager distributedLockManager,
                        RedissonClient redissonClient,
                        RedisCommonsProperties redisCommonsProperties,
                        LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "redis_lock_operations", "配置Redis锁操作");
                return new RedisLockOperationsImpl(
                                redisTemplate,
                                transactionManager,
                                executor,
                                resultProcessor,

                                logUtils,
                                distributedLockManager,
                                redissonClient,
                                redisCommonsProperties);
        }

        /**
         * 配置缓存键生成器
         *
         * @return CacheKeyGenerator实例
         */
        @Bean
        @ConditionalOnMissingBean(CacheKeyGenerator.class)
        public CacheKeyGenerator cacheKeyGenerator() {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "cache_key_generator", "配置缓存键生成器");
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
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "cache_condition_evaluator", "配置缓存条件评估器");
                return new CacheConditionEvaluator();
        }

        /** 配置批量操作管理器 */
        @Bean
        @ConditionalOnBean(RedisUtils.class)
        @ConditionalOnMissingBean(BatchOperationManager.class)
        public BatchOperationManager batchOperationManager(RedisUtils redisUtils, LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event(
                                                "redis_commons",
                                                "bean_creation",
                                                "batch_operation_manager",
                                                "配置Redis批量操作管理器");
                return new BatchOperationManager(redisUtils, logUtils);
        }

        /** 配置缓存预热管理器 */
        @Bean
        @ConditionalOnBean({ RedisUtils.class, BatchOperationManager.class })
        @ConditionalOnMissingBean(CacheWarmUpManager.class)
        public CacheWarmUpManager cacheWarmUpManager(
                        RedisUtils redisUtils, BatchOperationManager batchManager, LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "cache_warmup_manager", "配置Redis缓存预热管理器");
                return new CacheWarmUpManager(redisUtils, batchManager, logUtils);
        }

        /** 配置缓存性能监控收集器 */
        @Bean
        @ConditionalOnMissingBean(CacheMetricsCollector.class)
        public CacheMetricsCollector cacheMetricsCollector(LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event(
                                                "redis_commons",
                                                "bean_creation",
                                                "cache_metrics_collector",
                                                "配置Redis缓存性能监控收集器");
                return new CacheMetricsCollector(logUtils);
        }

        /**
         * 配置缓存切面
         *
         * @param chainManager 切面处理器链管理器
         * @return CacheAspect实例
         */
        @Bean
        @ConditionalOnBean(AspectChainManager.class)
        @ConditionalOnMissingBean(CacheAspect.class)
        public CacheAspect cacheAspect(AspectChainManager chainManager, LogUtils logUtils) {
                this.logUtils
                                .business()
                                .event("redis_commons", "bean_creation", "cache_aspect", "配置Redis缓存切面");
                return new CacheAspect(chainManager, logUtils);
        }
}
