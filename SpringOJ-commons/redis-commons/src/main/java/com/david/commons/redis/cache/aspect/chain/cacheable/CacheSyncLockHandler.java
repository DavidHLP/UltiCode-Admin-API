package com.david.commons.redis.cache.aspect.chain.cacheable;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.aspect.operator.CacheReadOperator;
import com.david.commons.redis.cache.aspect.operator.CacheKeyBuilder;
import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.lock.DistributedLockManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存同步锁处理器
 *
 * <p>当缓存未命中且配置 sync=true 时，使用分布式锁保护方法执行，
 * 通过加锁+二次检查避免缓存击穿，减少对数据库的并发访问。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSyncLockHandler extends Handler {

    private final DistributedLockManager lockManager;
    private final RedisCommonsProperties properties;
    private final CacheReadOperator readOperator;
    private final CacheKeyBuilder keyBuilder;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 已命中或无元数据，直接下个处理器
        if (aspectContext.getMetadata() == null || aspectContext.getCacheHit()) {
            executeHandle(aspectContext);
            return;
        }

        // 仅当配置 sync=true 时启用同步锁
        if (!aspectContext.getMetadata().sync()) {
            executeHandle(aspectContext);
            return;
        }

        // 基于缓存键构造锁键
        String cacheKey = null;
        try {
            cacheKey = keyBuilder.buildCacheKey(aspectContext.getMetadata(), aspectContext.getContext(), null);
        } catch (Exception e) {
            log.warn("构建缓存键失败，无法使用同步锁，直接继续：keyPattern={}", aspectContext.getMetadata().key(), e);
            executeHandle(aspectContext);
            return;
        }
        String lockKey = "cache-sync:" + cacheKey; // 最终会在 LockManager 内部追加全局前缀和 lock: 命名空间

        long waitTime = properties.getLock().getDefaultWaitTime();
        long leaseTime = properties.getLock().getDefaultLeaseTime();

        boolean acquired = false;
        try {
            acquired = lockManager.tryLock(lockKey, waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                // 未拿到锁：可能其他请求正在重建缓存，做一次二次检查
                Object cached = safeReadCache(aspectContext);
                if (cached != null) {
                    aspectContext.setCacheHit(cached);
                    return; // 命中后终止责任链
                }
                // 仍未命中：为防止并发击穿，默认不再执行原方法，直接结束责任链（返回空或由上层做降级）
                // 但如果返回类型为原始类型（primitive），为了避免返回 null 造成 NPE，这里继续后续处理
                Class<?> rt = aspectContext.getMetadata().returnType();
                if (rt != null && rt.isPrimitive()) {
                    log.debug("未获取到缓存同步锁且二次检查未命中，但返回类型为原始类型，继续后续处理以避免返回 null: {}", lockKey);
                    executeHandle(aspectContext);
                } else {
                    log.debug("未获取到缓存同步锁且二次检查未命中，短路返回空结果以防止并发击穿: {}", lockKey);
                    // 标记方法已"执行"但结果为 null，使切面在不真正回源的情况下结束并返回 null
                    aspectContext.setMethodExecuted(null);
                }
                return;
            }

            // 已拿到锁：二次检查，避免重复回源
            Object cached = safeReadCache(aspectContext);
            if (cached != null) {
                aspectContext.setCacheHit(cached);
                return; // 已命中直接结束
            }

            // 仍未命中：持锁执行原方法
            log.debug("缓存未命中，持锁执行原方法，lockKey={}", lockKey);
            actionMethodInvoked(aspectContext);

            // 交给后续处理器（通常是写入缓存）
            executeHandle(aspectContext);
        } finally {
            try {
                if (acquired && lockManager.isHeldByCurrentThread(lockKey)) {
                    lockManager.unlock(lockKey);
                }
            } catch (Exception e) {
                log.warn("释放缓存同步锁失败：{}", lockKey, e);
            }
        }
    }

    private Object safeReadCache(AspectContext aspectContext) {
        try {
            return readOperator.readCache(aspectContext.getMetadata(), aspectContext.getContext());
        } catch (Exception e) {
            log.warn("二次检查读取缓存失败，降级为未命中，keyPattern={}", aspectContext.getMetadata().key(), e);
            return null;
        }
    }
}
