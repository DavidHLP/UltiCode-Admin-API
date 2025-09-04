package com.david.commons.redis.cache.aspect.chain.cacheable;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.aspect.operator.CacheKeyBuilder;
import com.david.commons.redis.cache.aspect.operator.CacheTtlManager;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.cache.protection.interfaces.CacheBreakdownProtection;
import com.david.commons.redis.cache.protection.interfaces.CacheProtectionService;
import com.david.commons.redis.cache.protection.interfaces.CacheAvalancheProtection;
import com.david.commons.redis.operations.RedisStringOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 使用 CacheProtectionService 统一执行穿透/击穿/雪崩防护的处理器
 *
 * <p>放在 CacheReadHandler 之后，一旦未命中则由本处理器负责：
 * 1) 构建缓存键
 * 2) 封装数据加载器（调用原方法）
 * 3) 构造缓存写入器（TTL 由 CacheTtlManager 计算，雪崩抖动由 CacheAvalancheProtection 计算）
 * 4) 交由 CacheProtectionService 执行完整保护
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheProtectionHandler extends Handler {

    private final CacheProtectionService cacheProtectionService;
    private final CacheKeyBuilder keyBuilder;
    private final CacheTtlManager ttlManager;
    private final RedisUtils redisUtils;
    private final CacheExpressionEvaluator expressionEvaluator;
    private final CacheAvalancheProtection avalancheProtection;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 前置短路：无元数据或缓存已命中，交给下一个处理器（通常直接结束）
        if (aspectContext.getMetadata() == null || aspectContext.getCacheHit()) {
            executeHandle(aspectContext);
            return;
        }

        final CacheMetadata metadata = aspectContext.getMetadata();
        final CacheContext ctx = aspectContext.getContext();

        // 1) 构建缓存键（不依赖 result）
        final String cacheKey;
        try {
            cacheKey = keyBuilder.buildCacheKey(metadata, ctx, null);
        } catch (Exception e) {
            log.warn("构建缓存键失败，直接执行原方法并结束：keyPattern={}", metadata.key(), e);
            actionMethodInvoked(aspectContext);
            aspectContext.setIsEnd(true);
            return;
        }

        // 2) 封装数据加载器：调用原方法 + 回写上下文结果
        Supplier<Object> dataLoader = () -> {
            if (aspectContext.getMethodInvoked()) {
                return aspectContext.getMethodResult();
            }
            try {
                Object result = aspectContext.getJoinPoint().proceed();
                aspectContext.setMethodExecuted(result);
                return result;
            } catch (Throwable t) {
                log.error("受保护的数据加载执行异常：{}", ctx.getMethodName(), t);
                if (t instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(t);
            }
        };

        // 3) 构造缓存写入器：尊重 cacheNull/unless，按序列化与 TTL 策略写入，并应用雪崩抖动
        CacheBreakdownProtection.CacheWriter<Object> cacheWriter = (key, value, ttlFromUpstream) -> {
            // 3.1 unless 判断（true 表示跳过缓存）
            String unless = metadata.unless();
            if (unless != null && !unless.isEmpty()) {
                try {
                    boolean skip = expressionEvaluator.evaluateCondition(unless, ctx, value);
                    if (skip) {
                        log.debug("unless 条件满足，跳过缓存写入，键: {}", key);
                        return;
                    }
                } catch (Exception e) {
                    log.warn("评估 unless 条件发生异常，跳过缓存写入：{}", unless, e);
                    return;
                }
            }

            // 3.2 空值缓存策略
            if (value == null && !metadata.cacheNull()) {
                log.debug("跳过缓存空值，键: {}", key);
                return;
            }

            // 3.3 计算基础 TTL（包含空值 TTL 与默认 TTL 逻辑）
            long baseTtl = ttlManager.determineTtl(metadata, value);

            // 3.4 应用雪崩抖动：仅对 >0 的 TTL 生效（0 表示永不过期，不做抖动）
            long finalTtl = baseTtl;
            if (finalTtl > 0) {
                finalTtl = avalancheProtection.calculateRandomTtl(baseTtl);
            }

            // 3.5 写入 Redis（序列化与 TTL 与 CacheWriteOperator 保持一致）
            try {
                RedisStringOperations stringOps = redisUtils.string().using(metadata.serialization());
                if (finalTtl > 0) {
                    stringOps.set(key, value, finalTtl, TimeUnit.SECONDS);
                    log.debug("缓存写入成功，键: {}，TTL: {} 秒", key, finalTtl);
                } else if (finalTtl == 0) {
                    stringOps.set(key, value);
                    log.debug("缓存写入成功，键: {}，永不过期", key);
                } else {
                    long defaultTtl = ttlManager.getDefaultTtl();
                    stringOps.set(key, value, defaultTtl, TimeUnit.SECONDS);
                    log.debug("缓存写入成功，键: {}，使用默认 TTL: {} 秒", key, defaultTtl);
                }
            } catch (Exception e) {
                log.warn("写入缓存失败，键: {}", key, e);
            }
        };

        // 4) 组装防护配置并执行
        CacheProtectionService.ProtectionConfig<Object> config =
                new CacheProtectionService.ProtectionConfig<>(cacheKey, dataLoader, cacheWriter)
                        .setBloomFilterName(cacheKey) // Bloom 名默认与 key 一致
                        .setEnablePenetrationProtection(true)
                        .setEnableBreakdownProtection(metadata.sync()) // 遵循注解 sync
                        .setEnableAvalancheProtection(false); // 由本处理器的 writer 负责抖动，避免重复

        Object result = cacheProtectionService.executeWithFullProtection(config);

        // 5) 结束责任链并返回结果（若 dataLoader 未执行，也返回 service 的结果）
        if (!aspectContext.getMethodInvoked()) {
            aspectContext.setMethodExecuted(result);
        }
        aspectContext.setIsEnd(true);
    }
}
