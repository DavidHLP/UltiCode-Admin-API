package com.david.commons.redis.cache.fallback;

import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.CacheContext;
import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisCacheException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存降级处理器
 * <p>
 * 当缓存操作失败时，提供降级策略和异常处理机制。
 * 支持多种降级策略：直接执行原方法、本地缓存降级、熔断机制等。
 * </p>
 *
 * @author David
 */
@Component
@Slf4j
public class CacheFallbackHandler {

    private final RedisCommonsProperties properties;
    private final ConcurrentHashMap<String, Object> localCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> localCacheTimestamps = new ConcurrentHashMap<>();
    private final AtomicLong fallbackCount = new AtomicLong(0);
    private final AtomicLong circuitBreakerOpenTime = new AtomicLong(0);

    public CacheFallbackHandler(RedisCommonsProperties properties) {
        this.properties = properties;
    }

    /**
     * 处理缓存操作异常的降级逻辑
     *
     * @param joinPoint 切点
     * @param metadata  缓存元数据
     * @param context   缓存上下文
     * @param exception 异常信息
     * @return 降级后的结果
     */
    public Object handleCacheFallback(ProceedingJoinPoint joinPoint,
            CacheMetadata metadata,
            CacheContext context,
            Exception exception) throws Throwable {

        fallbackCount.incrementAndGet();

        log.warn("Cache operation failed, applying fallback strategy. Method: {}, Error: {}",
                context.getMethod().getName(), exception.getMessage());

        // 检查熔断器状态
        if (isCircuitBreakerOpen()) {
            log.debug("Circuit breaker is open, using local cache or direct execution");
            return handleCircuitBreakerFallback(joinPoint, metadata, context);
        }

        // 根据配置的降级策略处理
        FallbackStrategy strategy = determineFallbackStrategy(metadata, exception);

        switch (strategy) {
            case LOCAL_CACHE:
                return handleLocalCacheFallback(joinPoint, metadata, context);
            case DIRECT_EXECUTION:
                return handleDirectExecutionFallback(joinPoint, context);
            case CIRCUIT_BREAKER:
                return handleCircuitBreakerActivation(joinPoint, metadata, context);
            default:
                return handleDirectExecutionFallback(joinPoint, context);
        }
    }

    /**
     * 处理缓存查询失败的降级
     */
    public Object handleCacheableFallback(ProceedingJoinPoint joinPoint,
            CacheMetadata metadata,
            CacheContext context,
            Exception exception) throws Throwable {

        log.debug("Cacheable operation failed, checking local cache for key pattern: {}", metadata.getKey());

        // 尝试从本地缓存获取
        String localKey = buildLocalCacheKey(metadata, context);
        Object localValue = getFromLocalCache(localKey);

        if (localValue != null) {
            log.debug("Found value in local cache for key: {}", localKey);
            return localValue;
        }

        // 本地缓存未命中，执行原方法
        Object result = joinPoint.proceed();

        // 将结果存入本地缓存作为备份
        putToLocalCache(localKey, result, metadata.getTtl());

        return result;
    }

    /**
     * 处理缓存更新失败的降级
     */
    public void handleCachePutFallback(CacheMetadata metadata,
            CacheContext context,
            Exception exception) {

        log.debug("Cache put operation failed, storing in local cache for key pattern: {}", metadata.getKey());

        // 将值存储到本地缓存
        String localKey = buildLocalCacheKey(metadata, context);
        Object result = context.getResult();
        putToLocalCache(localKey, result, metadata.getTtl());
    }

    /**
     * 处理缓存清除失败的降级
     */
    public void handleCacheEvictFallback(CacheMetadata metadata,
            CacheContext context,
            Exception exception) {

        log.debug("Cache evict operation failed, clearing local cache for key pattern: {}", metadata.getKey());

        // 清除本地缓存
        String localKey = buildLocalCacheKey(metadata, context);

        if (metadata.isAllEntries()) {
            // 清除所有匹配的本地缓存项
            clearLocalCacheByPattern(localKey);
        } else {
            // 清除单个本地缓存项
            removeFromLocalCache(localKey);
        }
    }

    /**
     * 确定降级策略
     */
    private FallbackStrategy determineFallbackStrategy(CacheMetadata metadata, Exception exception) {
        // 如果启用了本地缓存降级
        if (properties.getCache().isEnableLocalCacheFallback()) {
            return FallbackStrategy.LOCAL_CACHE;
        }

        // 如果启用了熔断器且错误率过高
        if (properties.getProtection().isEnableCircuitBreaker() && shouldActivateCircuitBreaker(exception)) {
            return FallbackStrategy.CIRCUIT_BREAKER;
        }

        // 默认直接执行原方法
        return FallbackStrategy.DIRECT_EXECUTION;
    }

    /**
     * 处理本地缓存降级
     */
    private Object handleLocalCacheFallback(ProceedingJoinPoint joinPoint,
            CacheMetadata metadata,
            CacheContext context) throws Throwable {

        String localKey = buildLocalCacheKey(metadata, context);
        Object localValue = getFromLocalCache(localKey);

        if (localValue != null) {
            log.debug("Using local cache value for key: {}", localKey);
            return localValue;
        }

        // 执行原方法并缓存到本地
        Object result = joinPoint.proceed();
        putToLocalCache(localKey, result, metadata.getTtl());

        return result;
    }

    /**
     * 处理直接执行降级
     */
    private Object handleDirectExecutionFallback(ProceedingJoinPoint joinPoint,
            CacheContext context) throws Throwable {

        log.debug("Executing method directly without cache for: {}", context.getMethod().getName());
        return joinPoint.proceed();
    }

    /**
     * 处理熔断器激活
     */
    private Object handleCircuitBreakerActivation(ProceedingJoinPoint joinPoint,
            CacheMetadata metadata,
            CacheContext context) throws Throwable {

        log.warn("Activating circuit breaker for cache operations");
        circuitBreakerOpenTime.set(System.currentTimeMillis());

        return handleLocalCacheFallback(joinPoint, metadata, context);
    }

    /**
     * 处理熔断器开启状态的降级
     */
    private Object handleCircuitBreakerFallback(ProceedingJoinPoint joinPoint,
            CacheMetadata metadata,
            CacheContext context) throws Throwable {

        // 检查是否应该尝试半开状态
        long openTime = circuitBreakerOpenTime.get();
        long circuitBreakerTimeout = properties.getProtection().getCircuitBreakerTimeout();

        if (System.currentTimeMillis() - openTime > circuitBreakerTimeout) {
            log.debug("Circuit breaker timeout reached, attempting half-open state");
            circuitBreakerOpenTime.set(0); // 重置熔断器
        }

        return handleLocalCacheFallback(joinPoint, metadata, context);
    }

    /**
     * 判断是否应该激活熔断器
     */
    private boolean shouldActivateCircuitBreaker(Exception exception) {
        // 简单的熔断器逻辑：连续失败次数超过阈值
        long currentFailures = fallbackCount.get();
        long threshold = properties.getProtection().getCircuitBreakerThreshold();

        return currentFailures > threshold;
    }

    /**
     * 检查熔断器是否开启
     */
    private boolean isCircuitBreakerOpen() {
        long openTime = circuitBreakerOpenTime.get();
        return openTime > 0;
    }

    /**
     * 构建本地缓存键
     */
    private String buildLocalCacheKey(CacheMetadata metadata, CacheContext context) {
        // 简化的键构建逻辑，实际应该使用表达式求值器
        return "local:" + metadata.getKey() + ":" + context.getMethod().getName();
    }

    /**
     * 从本地缓存获取值
     */
    private Object getFromLocalCache(String key) {
        Long timestamp = localCacheTimestamps.get(key);
        if (timestamp == null) {
            return null;
        }

        // 检查是否过期
        long localCacheTtl = properties.getCache().getLocalCacheTtl();
        if (localCacheTtl > 0 && System.currentTimeMillis() - timestamp > localCacheTtl * 1000) {
            removeFromLocalCache(key);
            return null;
        }

        return localCache.get(key);
    }

    /**
     * 存储到本地缓存
     */
    private void putToLocalCache(String key, Object value, long ttl) {
        if (value == null) {
            return;
        }

        localCache.put(key, value);
        localCacheTimestamps.put(key, System.currentTimeMillis());

        // 限制本地缓存大小
        int maxSize = properties.getCache().getLocalCacheMaxSize();
        if (localCache.size() > maxSize) {
            evictOldestLocalCacheEntries(maxSize / 2);
        }
    }

    /**
     * 从本地缓存移除
     */
    private void removeFromLocalCache(String key) {
        localCache.remove(key);
        localCacheTimestamps.remove(key);
    }

    /**
     * 根据模式清除本地缓存
     */
    private void clearLocalCacheByPattern(String pattern) {
        localCache.entrySet().removeIf(entry -> entry.getKey().matches(pattern));
        localCacheTimestamps.entrySet().removeIf(entry -> entry.getKey().matches(pattern));
    }

    /**
     * 清除最旧的本地缓存条目
     */
    private void evictOldestLocalCacheEntries(int count) {
        localCacheTimestamps.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                .limit(count)
                .forEach(entry -> {
                    String key = entry.getKey();
                    localCache.remove(key);
                    localCacheTimestamps.remove(key);
                });
    }

    /**
     * 获取降级统计信息
     */
    public FallbackMetrics getFallbackMetrics() {
        return FallbackMetrics.builder()
                .fallbackCount(fallbackCount.get())
                .localCacheSize(localCache.size())
                .circuitBreakerOpen(isCircuitBreakerOpen())
                .circuitBreakerOpenTime(circuitBreakerOpenTime.get())
                .build();
    }

    /**
     * 重置降级统计
     */
    public void resetFallbackMetrics() {
        fallbackCount.set(0);
        circuitBreakerOpenTime.set(0);
    }

    /**
     * 清空本地缓存
     */
    public void clearLocalCache() {
        localCache.clear();
        localCacheTimestamps.clear();
    }

    /**
     * 降级策略枚举
     */
    private enum FallbackStrategy {
        LOCAL_CACHE, // 本地缓存降级
        DIRECT_EXECUTION, // 直接执行原方法
        CIRCUIT_BREAKER // 熔断器激活
    }
}